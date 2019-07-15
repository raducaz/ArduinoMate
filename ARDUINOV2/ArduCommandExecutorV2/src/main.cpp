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
int f2()
{
  // To ensure temp can be read using pins 0 and 1 need to Serial.end();
  float t = dht.readTemperature();
  
  // Check if any reads failed and exit early (to try again).
  if (isnan(t)) {
    return -100;
  }
  return (int)t;
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
void setAnalogPin(byte pin, int state)
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

void setAnalogPinTemp(byte pin, int state, unsigned int interval)
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
char* appendCmdResult(char* res, char* cmd, int value)
{
  char* result;
  char buffer [17]; //max int on 16bit processor
  char* sVal = itoa(value, buffer, 10);
  size_t resLen = strlen(res);
  size_t cmdLen = strlen(cmd);
  size_t valLen = strlen(sVal);
  if((result = (char*) malloc(cmdLen+resLen+valLen+1+1+1)) != NULL)
  {
    result[0]='\0';
    strcat(result,res);
    strcat(result,"|");
    strcat(result,cmd);
    strcat(result,":");
    strcat(result,sVal);
  }
  else
  {
    Logger::debugln("Error malloc");
  }
  
  free(res); //deallocate old pointer
  res=NULL;
  return result;

  // size_t len = strlen(res);
  // if(len>0){
  //   res[len] = '|';
  //   res[len+1] = '\0';
  // }
  // strcat(res, cmd); //append cmd in res
  // len = strlen(res);
  // res[len] = ':';
  // res[len+1] = '\0';
  // char buffer [5]; //reads cannot be more than 4 chars

  // strcat(res, itoa(value, buffer, 10));
}
int getPin(const char* key)
{
  char sPin[4]="";
  bool isAnalogPin = (key[0]=='A'||key[0]=='a');
  
  byte i= 0; //Start from second character id analog
  byte index = (isAnalogPin ? 1 : 0);
  while(key[index+i]){
    sPin[i]=key[index+i];
    i++;
  }
  sPin[i] = '\0';
  
  int pinNo = atoi(sPin);
  //return isAnalogPin ? (pinNo<2 ? pinNo+16 : pinNo+18) : pinNo;
  return isAnalogPin ? pinNo+14 : pinNo; //A0 is 14, A1 is 15...
}
int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin)
{
  byte i = 1; //First char is cmd char
  byte index = 0;
  char res[strlen(cmd)] = "";
  const char separator = ':';
  while(cmd[i]>0){

    if(cmd[i]==separator){
      index++;

      if(index>paramIndex)
        break;

    } else if(index==paramIndex){
      size_t len = strlen(res);
      res[len] = cmd[i];
      res[len + 1] = '\0';
    }

    i++;
  }

  if(returnAsPin)
    return getPin(res);

  return atoi(res);
}

char* parseCommand(char* plainJson)
{
  // [=3:0.25:2] - set analog 3 to 0.25 for 2 ms (setdigital if digital pin)
  // [~3:0.25:2] - set digital 3 to 0.25 for 2 ms 
  // [=3:0.25] - set analog 2 to ...
  // [~3:0.25] - set digital
  // [!:20] - wait for 20 ms
  // [?:3] - digital read (including analog pins)
  // [#:3] - analog read if analog
  // [F0] - run f0()
  // [=3:0.25:2|!20|?:3]

Serial.println(freeMemory());

size_t len = strlen(plainJson);
char cmd[len] = "";
//char res[len] = ""; //return get pin values
char* res;

if(plainJson[0]=='['&&plainJson[len-1]==']')
{
  byte i = 0;
  while(plainJson[i]>0)
  {
    Serial.print(plainJson[i]);

    if(plainJson[i]=='|' || plainJson[i]==']') // cmd terminator
    {
        Serial.println(cmd);

        byte pin = getCmdParam(cmd,0, true);
        int value = getCmdParam(cmd,1, false);
        int interval = getCmdParam(cmd,2, false);

      Serial.print("pin");Serial.print(pin);
      Serial.print("value");Serial.print(value);
      Serial.print("interv");Serial.println(interval);

        // execute
        if(cmd[0]=='=' || 
              cmd[0]=='~') //Cmd set
        {
          if(interval>0){ // Cmd set temp for x ms
            if(cmd[0]=='~') 
            { 
              setDigitalPinTemp(pin,value,interval);
            }
            else
            {
              if(pin<=13) setDigitalPinTemp(pin,value,interval);
              else setAnalogPinTemp(pin,value,interval);  
            }
          }
          else // Cmd permanent set
          {
            if(cmd[0]=='~') 
            {
              setDigitalPin(pin, value);
            }
            else
            {
              if(pin<=13) setDigitalPin(pin, value);
              else setAnalogPin(pin, value);
            }
          }
        }
        
        if(cmd[0]=='?'){
          res = appendCmdResult(res, cmd, digitalRead(pin));
        }
        if(cmd[0]=='#'){
          res = appendCmdResult(res, cmd, (pin<=13 ? digitalRead(pin) : analogRead(pin)));
        }
        if(cmd[0]=='!'){ // Cmd wait
          wait(value);
        }
        if(cmd[0]=='F'){ // Cmd function
          if(pin==0)
            f0();
          if(pin==1)
            res = appendCmdResult(res,cmd,f1());
          if(pin==2)
            res = appendCmdResult(res,cmd,f2());
        }
        
        // Clear received temp to be prepared to receive next command
        strcpy(cmd, "\0");
    }
    else{
      if(plainJson[i]!='['){
        size_t len = strlen(cmd);
        cmd[len] = plainJson[i];
        cmd[len + 1] = '\0';
      }
    }

    i++;
  }
  
} else {
  strcat(res, "Error parsing message");
  Logger::debugln("Deserialize received message failed.");
}

//strcpy(plainJson, res);
return res;

Serial.println(freeMemory());

}
void listenSerial()
{
  char buffer[MAXBUFFERSIZE] = ""; 
  unsigned int bufferSize = 0;

  Serial.println("Listen Serial");
  char endChar = '\n';
  const byte SerialSize = 64; // This is Serial.read limit
  
  if(Serial){

    if (Serial.available()!=0) {
      Serial.println("Serial available");

      char c = 0;
      bool endCmd = false;
      byte i=0;
      while(((c=Serial.read())!=endChar) && i<SerialSize ){

        Serial.print(c);

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
        char* result = parseCommand(buffer);//this fills buffer with results

        Logger::debugln(result);
        
        free(result);
        result = NULL;
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
  char receivedText[MAXBUFFERSIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
  
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
          parseCommand(receivedText); //Fills receivesText with results
          client.println(receivedText);
          Serial.println(receivedText);
         
          // Clear received temp to be prepared to receive next command
          strcpy(receivedText, "\0");
        } 
        else
        {
          size_t len = strlen(receivedText);
          if (len < MAXBUFFERSIZE-1)
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
