#include <Arduino.h>
#include <Thread.h>
#include <ThreadController.h>
// #include <TimerOne.h>

#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include "globals.h"
#include <SPI.h>
#include <ACS712.h>
#include <configuration.h>
#include "logger.h"
#include <jsonhelper.cpp>
#include <executor.h>

#include "Adafruit_Sensor.h"
#include "DHT.h"

#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321
DHT dht(9, DHTTYPE);

volatile byte DeviceState = 0; // 0=READY,1=BUSY,2=ERROR

ThreadController threadsController = ThreadController();
Thread serverThread = Thread();
Thread clientThread = Thread();

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A1);
float zeroCurrent = 0;

static const unsigned int MAXBUFFERSIZE = 200;
char buffer[MAXBUFFERSIZE] = ""; 
unsigned int bufferSize = 0;

EthernetClient arduinoClient;

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
float f1()
{
  return sensor.getCurrentAC()-zeroCurrent;
}
float f2()
{
  float t = dht.readTemperature();
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
void parseCommand(String plainJson, EthernetClient client)
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

    // Parse JSON object
    JsonArray& arr = jb.parseArray(plainJson);

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
                  MyExecutor::setDigitalPinTemp(pin,p.value,obj["@"].as<int>());
                }
                else
                {
                  if(pin<=13) MyExecutor::setDigitalPinTemp(pin,p.value,obj["@"].as<int>());
                  else MyExecutor::setAnalogPinTemp(pin,p.value,obj["@"].as<float>());  
                }
              }
              else // Cmd permanent set
              {
                if(strncmp(p.key,"~",1)==0) 
                {
                  MyExecutor::setDigitalPin(pin, p.value.as<int>());
                }
                else
                {
                  if(pin<=13) MyExecutor::setDigitalPin(pin, p.value.as<int>());
                  else MyExecutor::setAnalogPin(pin, p.value.as<float>());
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
              MyExecutor::wait(p.value);
            }
            if(strcmp(p.key,"F1")==0){ // Cmd function
              obj[p.key] = f1();
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

    if(client)
      arr.printTo(client);
    arr.printTo(Serial);
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
        parseCommand(buffer, 0);
        
        buffer[0]=0;
        bufferSize=0;
      }
    }
  }
}
void listenEthernet()
{
  //Serial.println("Start Server to listen clients...");
  EthernetServer server = EthernetServer(arduinoPort);
  server.begin();

  EthernetClient client = server.available();
  Logger::debugln("Server started...listening...");

  char endChar = '\n';
  String receivedText = "";
      
  if (client) 
  {
    client.setTimeout(10000); //reads input for 10 seconds or until endChar is reached
    if (client.connected()) 
    {
      Logger::debugln("Client connected");

      if (client.available()) 
      {
        receivedText = client.readStringUntil(endChar);
        Logger::debugln(receivedText);
        Serial.println(receivedText);

        //!!! this executes actual commands and blocks the thread until done
        // No other connections are permitted during this processing
        parseCommand(receivedText, client);
        
      }
      
    }

    Logger::debugln("");
    Logger::debugln("CLOSE CONNECTION"); 
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
  int i = 0;
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
    // Send the state of the pins
    float digitalPinStates[14];
    for(byte i=0;i<14;i++)
    {
        digitalPinStates[i] = digitalRead(i);
    }
    MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(arduinoName, 0, 0, digitalPinStates, 14),arduinoClient);

    float analogPinStates[6];
    for(byte i=0;i<=5;i++)
    {
        analogPinStates[i] = analogRead(i+14);
    }
    
    //--------DEVICE SPECIFIC---------------------------
    analogPinStates[1] = f1();
    //--------DEVICE SPECIFIC---------------------------

    MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(arduinoName, 0, 1, analogPinStates, 6),arduinoClient);
    MyExecutor::sendToServer("END",arduinoClient);
  }
}

void setupThread()
{
  serverThread.onRun(serverThreadCallback);
	serverThread.setInterval(1000);

  clientThread.onRun(clientThreadCallback);
	clientThread.setInterval(5000);

  threadsController.add(&serverThread);
  threadsController.add(&clientThread);

  // Timer1.initialize(20000); // in useconds
	// Timer1.attachInterrupt(timerCallback);
	// Timer1.start();
}
void setup() {
  // put your setup code here, to run once:

  
  Serial.begin(9600);
  Logger::logln("Entering Setup");

  Configuration::setupPins();
  Configuration::initializePins();

  delay(1000);
  calibrateCurrentSensor();
  dht.begin();

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);

  delay(1000);
  Logger::log("My IP address: ");
  Logger::logln(Ethernet.localIP());
  
  setupThread();
}
void loop() {
  
  delay(500);
  //Start the Thread in loop
  threadsController.run();

  // Send ImAlive to WatchDog
  Logger::logln("I'm alive !");
  digitalWrite(Configuration::WatchDog, digitalRead(Configuration::WatchDog)==0?1:0);
  delay(500);


  // // Reading temperature or humidity takes about 250 milliseconds!
  // // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  // float h = dht.readHumidity();
  // // Read temperature as Celsius (the default)
  // float t = dht.readTemperature();
  // // Read temperature as Fahrenheit (isFahrenheit = true)
  // float f = dht.readTemperature(true);

  // // Check if any reads failed and exit early (to try again).
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
