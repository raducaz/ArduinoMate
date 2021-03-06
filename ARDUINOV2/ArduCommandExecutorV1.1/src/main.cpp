#include <Arduino.h>

#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include "globals.h"
#include <SPI.h>
#include <ACS712.h>
#include <configuration.h>
#include "logger.h"
#include <ArduinoJson.h>

#include "Adafruit_Sensor.h"
#include "DHT.h"

#ifdef __arm__
// should use uinstd.h to define sbrk but Due causes a conflict
extern "C" char* sbrk(int incr);
#else  // __ARM__
extern char *__brkval;
#endif  // __arm__

int freeMemory() {
  char top;
#ifdef __arm__
  return &top - reinterpret_cast<char*>(sbrk(0));
#elif defined(CORE_TEENSY) || (ARDUINO > 103 && ARDUINO != 151)
  return &top - __brkval;
#else  // __arm__
  return __brkval ? &top - __brkval : &top - __malloc_heap_start;
#endif  // __arm__
}

#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321
DHT dht(Configuration::TemperatureSensor, DHTTYPE);

byte noLoopRuns = 0;
bool isRestarted = true;
const char PIN[4] = "pin";
const char NAME[5] = "name";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char MSG[4] = "msg";
const char DIGITAL[12] = "digitalPins";
const char ANALOG[11] = "analogPins";

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A1);
float zeroCurrent = 0;

static const unsigned int MAXBUFFERSIZE = 200;
char buffer[MAXBUFFERSIZE] = ""; 
unsigned int bufferSize = 0;

EthernetClient arduinoClient;
EthernetServer server = EthernetServer(arduinoPort);

// Reset function
void(* resetFunc) (void) = 0;

void calibrateCurrentSensor()
{
  // calibrate() method calibrates zero point of sensor,
  // It is not necessary, but may positively affect the accuracy
  // Ensure that no current flows through the sensor at this moment
  // If you are not sure that the current through the sensor will not leak during calibration - comment out this method
  sensor.calibrate();

  float c = 0;
  for(int i=0;i<10;i++)
  {
    c += sensor.getCurrentAC();
    delay(100);
  }
  zeroCurrent = c / 10.0;
}
void f0()
{
  resetFunc();
}
float f1()
{
  return sensor.getCurrentAC()-zeroCurrent;
}
float f2()
{
  Serial.end();
  delay(100);

  // To ensure temp can be read using pins 0 and 1
  float t = dht.readTemperature();
  Serial.begin(9600);

  // Check if any reads failed and exit early (to try again).
  if (isnan(t)) {
    return -100;
  }
  return t;
}

// This is the callback for the Timer
// void timerCallback(){
// 	threadsController.run();
// }

void wait(unsigned int msInterval)
{
    Logger::debug("wait for ");Logger::debug(msInterval);Logger::debugln(" ms");
    unsigned long waitStart = millis();

    unsigned long current = millis();
    while((current - waitStart)< msInterval) 
    {
      current = millis();
    }; 
}  

void sendToServer(const char* msg)
{
  arduinoClient.println(msg);

  Logger::debugln(msg);
}
void setDigitalPin(byte pin, byte state)
{
  Logger::debug("setPin:"); Logger::debug(pin);Logger::debug(" to ");Logger::debugln(state);
  digitalWrite(pin, state);
}
void setAnalogPin(byte pin, float state)
{
  Logger::debug("setPin:"); Logger::debug(pin);Logger::debug(" to ");Logger::debugln(state);
  analogWrite(pin, state);
}
void setDigitalPinTemp(byte pin, byte state, unsigned int interval)
{
  Logger::debug("setPinTemp:");Logger::debug(pin);Logger::debug(" to ");Logger::debug(state);
  Logger::debug(" for ");Logger::debugln(interval);

  byte state1 = digitalRead(pin);
  digitalWrite(pin, state);
  wait(interval);
  digitalWrite(pin, state1);
}

void setAnalogPinTemp(byte pin, float state, unsigned int interval)
{
  Logger::debug("setPinTemp:");Logger::debug(pin);Logger::debug(" to ");Logger::debug(state);
  Logger::debug(" for ");Logger::debugln(interval);

  int state1 = analogRead(pin);
  analogWrite(pin, state);
  wait(interval);
  analogWrite(pin, state1);
}

void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    const int* pinStates, byte size, 
    const char* msg)
  {
      StaticJsonBuffer<300> _buffer;
      
      Serial.println(freeMemory());

      JsonObject& _root = _buffer.createObject();
      if(strcmp(msg,"") != 0)
          _root[MSG] = msg;
      _root[NAME] = deviceName;
      _root[DEVICESTATE] = deviceState;

      Serial.println(freeMemory());

      JsonArray& psArr = _root.createNestedArray(pinType==0?DIGITAL:ANALOG);
      for(byte i=0;i<size;i++)
      {
          psArr.add(pinStates[i]);
      }

      Serial.println(freeMemory());

      _root.printTo(arduinoClient);
      arduinoClient.println();

Serial.println(freeMemory());

      _root.printTo(Serial);
      Serial.println();

      Serial.println(freeMemory());
  }
void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    int* pinStates, byte size)
  {
      constructPinStatesJSON(deviceName, deviceState, pinType, pinStates, size, "");
  }

int getPin(const byte startIndex, const char* key)
{
  char sPin[5]="";
  bool isAnalogPin = (key[startIndex]=='A'||key[startIndex]=='a');
  
  byte i= 0; //Start from second character id analog
  byte index = startIndex + (isAnalogPin ? 1 : 0);
  while(key[index+i]){
    sPin[i]=key[index+i];
    i++;
  }
  sPin[i] = '\0';
  
  int pinNo = atoi(sPin);
  //return isAnalogPin ? (pinNo<2 ? pinNo+16 : pinNo+18) : pinNo;
  return isAnalogPin ? pinNo+14 : pinNo; //A0 is 14, A1 is 15...
}
void parseCommand(char* plainJson, EthernetClient& client)
{
  // [
  //   {"=3":0.25,"@":2}, // for 2 ms and revert
  //   {"=13":0},//set indefinite
  //   {"!":20} // wait for 20 ms
  //   {"get/13":"?"}
  // ]
  // [{"=3":1,"@":2},{"?13":0},{"=13":0},{"?13":0},{"!":20}]
  // [set 3=1 for 2, get 13, set 13 to 0, get 13, wait 20]
    
    // static const int capacity = JSON_ARRAY_SIZE(10) + 10*JSON_OBJECT_SIZE(2) + 32;
    // StaticJsonBuffer<capacity> jb;
    
    DynamicJsonBuffer jb;
    //StaticJsonBuffer<300> jb;

Serial.println(freeMemory());

    // Parse JSON object
    JsonArray& arr = jb.parseArray(plainJson);

Serial.println(freeMemory());

    if (arr.success()) {
      for (JsonVariant& elem : arr) {
        if (elem.is<JsonObject>()) {
          JsonObject& obj = elem.as<JsonObject>();

          int pin=0;
          for (JsonPair& p : obj) {
            //p.key is a const char* pointing to the key
            if(strncmp(p.key,"=",1)==0 || 
                strncmp(p.key,"~",1)==0) //Cmd set
            {
              pin = getPin(1, p.key);
              if(obj["@"]){ // Cmd set temp for x ms
                if(strncmp(p.key,"~",1)==0) 
                { 
                  setDigitalPinTemp(pin,p.value,obj["@"].as<int>());
                }
                else
                {
                  if(pin<=13) setDigitalPinTemp(pin,p.value,obj["@"].as<int>());
                  else setAnalogPinTemp(pin,p.value,obj["@"].as<float>());  
                }
              }
              else // Cmd permanent set
              {
                if(strncmp(p.key,"~",1)==0) 
                {
                  setDigitalPin(pin, p.value.as<int>());
                }
                else
                {
                  if(pin<=13) setDigitalPin(pin, p.value.as<int>());
                  else setAnalogPin(pin, p.value.as<float>());
                }
              }
            }
            if(strncmp(p.key,"?",1)==0 || 
                strncmp(p.key,"#",1)==0) // Cmd get
            {
              pin = getPin(1, p.key);
              if(strncmp(p.key,"#",1)==0)
                obj[p.key] = digitalRead(pin);
              else
                obj[p.key] = pin<=13 ? digitalRead(pin) : analogRead(pin);
            }
            if(strcmp(p.key,"!")==0){ // Cmd wait
              wait(p.value);
            }
            if(strcmp(p.key,"F0")==0){ // Cmd function
              f0();
            }
            if(strcmp(p.key,"F1")==0){ // Cmd function
              obj[p.key] = f1();
            }
            if(strcmp(p.key,"F2")==0){ // Cmd function
              obj[p.key] = f2();
            }
          }
         
          obj[">"] = 1;
        }
      }

    } else {
      // parseObject() failed
      JsonArray& arr = jb.createArray();
      arr.add("Error parsing message");
      Logger::debugln("Deserialize received message failed.");
    }

Serial.println(freeMemory());
    if(client)
      arr.printTo(client);
    arr.printTo(Serial);

Serial.println(freeMemory());
}
void listenSerial()
{
  char endChar = '\n';
  const byte SerialSize = 64; // This is Serial.read limit
  
  if(Serial){

    if (Serial.available()!=0) {
      char c = 0;
      bool endCmd = false;
      byte i=0;
      while(((c=Serial.read())!=endChar) && i<SerialSize ){
        buffer[bufferSize]=c;
        buffer[bufferSize+1]='\0';
        bufferSize++;
        
        if(c==']' || bufferSize+1>=MAXBUFFERSIZE)
        {
          endCmd=true;
          break;
        }

        i++;
      }
        
      //Logger::debugln(receivedText.as<char*>());
      Serial.println("");
      Serial.println(buffer);

      //TODO: Test - this executes actual commands and blocks the thread until done
      if(endCmd)
      {
        //parseCommand(buffer, 0);
        
        buffer[0]=0;
        bufferSize=0;
      }
    }
  }
}
void listenEthernet()
{
  EthernetClient client = server.available();
  client.setTimeout(10000);

  char endChar = '\n';
  const byte SIZE = 250;
  char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
  
  if (client) 
  {
    Logger::debugln("Client connected");
    while (client.connected()) 
    {
      if (client.available()) 
      {
        char receivedChar = client.read();
        //Logger::debugln(receivedChar);

        if (receivedChar==endChar)
        {
          //!!!This blocks current thread until command is done
          parseCommand(receivedText, client);
         
          // Clear received temp to be prepared to receive next command
          strcpy(receivedText, "\0");
        }
        else
        {
          size_t len = strlen(receivedText);
          if (len < SIZE-1)
          {
            receivedText[len] = receivedChar;
            receivedText[len + 1] = '\0';
          }
          else
          {
            Serial.println("Max received message len reached.");
          }
        } 
      } 
    }

    Serial.println();
    Serial.println("CLOSE CONNECTION"); 
    client.stop();
  
  }
  else
  {
    Logger::debugln("No client, server stopped");
  }  
}
void serverThreadCallback()
{
    if(Configuration::useEthernet()){
      listenEthernet();
    }
    else
    {
      listenSerial();
    }
}
bool ConnectToServer(const byte* ip, const int port)
{
  if(arduinoClient)
  { 
    if(!arduinoClient.connected())
    {
      arduinoClient.stop();
      
      Logger::debugln("MON: Reconnecting...");
      if (arduinoClient.connect(ip, port)) {    
        return arduinoClient.connected();
      }
      else
      {
        return false;
      }
    }
    else
    {
      return true;
    }
    
  }
  else
  {
    Logger::debugln("MON: Connecting...");
    arduinoClient.connect(ip, port);
    return arduinoClient.connected();
  }
}
void clientThreadCallback()
{
  byte i = 0;
  while(!(arduinoClient.connected()) && (i < 2))
  {
    ConnectToServer(serverIp, serverPort);
    i++;
  }
  if(!arduinoClient.connected())
  {
    Logger::debugln("MON: Check connection to gateway.");
    if(!ConnectToServer(gateway, 80))
    {
      Logger::debugln("MON: Cannot connect, reinitialize ethernet.");
      Ethernet.begin(mac, ip, dns, gateway, subnet);
    }
  } else
  {
    // Send status all the time
    Logger::debug("MON: Sending status from ...");Logger::debugln(Ethernet.localIP());
    byte state = 0;
    if(isRestarted)
    { 
      state = 3;
    }
    // Send the state of the pins
    int digitalPinStates[14];
    for(byte i=0;i<14;i++)
    {
        digitalPinStates[i] = digitalRead(i);
    }
    constructPinStatesJSON(arduinoName, state, 0, digitalPinStates, 14);

    int analogPinStates[6];
    for(byte i=0;i<=5;i++)
    {
        analogPinStates[i] = analogRead(i+14);
    }
    
    //--------DEVICE SPECIFIC---------------------------
    analogPinStates[1] = f1();
    //--------DEVICE SPECIFIC---------------------------

    constructPinStatesJSON(arduinoName, state, 1, analogPinStates, 6);
  
    isRestarted=false;
    
    sendToServer("END");
  }
}

void setupThread()
{
  // serverThread.onRun(serverThreadCallback);
	// serverThread.setInterval(1000);

  // clientThread.onRun(clientThreadCallback);
	// clientThread.setInterval(5000);

  // threadsController.add(&serverThread);
  // threadsController.add(&clientThread);
}
void setup() {
  delay(250);
  
  // Sensor initialization
  calibrateCurrentSensor();
  dht.begin();
// Sensor initialization

  delay(1000);
  
  Serial.begin(9600);
  Logger::logln("Entering Setup");

  Configuration::setupPins();
  Configuration::initializePins();

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);

  delay(1000);
  Logger::log("My IP address: ");
  Logger::logln(Ethernet.localIP());
  
  server.begin();
  Logger::debugln("Server started...listening...");

  setupThread();
}
void loop() {
  
  delay(500);
  
  // Receive commands
  serverThreadCallback();
  delay(500);

  if(noLoopRuns < 5)
  {
    noLoopRuns++;
  }
  else
  {
    // send pin status
    clientThreadCallback();
    noLoopRuns = 0;
  }


  // Send ImAlive to WatchDog
  Logger::logln("I'm alive !");
  digitalWrite(Configuration::WatchDog, digitalRead(Configuration::WatchDog)==0?1:0);
  delay(500);
  Serial.println(f2());

  // // Reading temperature or humidity takes about 250 milliseconds!
  // // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  // float h = dht.readHumidity();
  // // Read temperature as Celsius (the default)
  // float t = dht.readTemperature();
  // // Read temperature as Fahrenheit (isFahrenheit = true)
  // float f = dht.readTemperature(true);

  // // // Check if any reads failed and exit early (to try again).
  // if (isnan(h) || isnan(t) || isnan(f)) {
  //   Serial.println(F("Failed to read from DHT sensor!"));
  //   return;
  // }

  // // Compute heat index in Fahrenheit (the default)
  // float hif = dht.computeHeatIndex(f, h);
  // // Compute heat index in Celsius (isFahreheit = false)
  // float hic = dht.computeHeatIndex(t, h, false);

  // Serial.print(F("Humidity: "));
  // Serial.print(h);
  // Serial.print(F("%  Temperature: "));
  // Serial.print(t);
  // Serial.print(F("°C "));
  // Serial.print(f);
  // Serial.print(F("°F  Heat index: "));
  // Serial.print(hic);
  // Serial.print(F("°C "));
  // Serial.print(hif);
  // Serial.println(F("°F"));

}
