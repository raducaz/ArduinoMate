
#include "tcpserver.h"

#include <ArduinoJson.h>
#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetServer.h>
#include <executor.h>
#include "logger.h"
#include "configuration.h"

MyTcpServerThread::MyTcpServerThread() {;}
MyTcpServerThread::~MyTcpServerThread() {;}

// Function executed on thread execution
void MyTcpServerThread::run(){

  if(DeviceState==0)
  {
    DeviceState = 1;
    if(Configuration::useEthernet()){
      this->listenEthernet();
    }
    else
    {
      this->listenSerial();
    }
  }
  
  DeviceState = 0;
  runned();
}
void MyTcpServerThread::listenSerial()
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
        JsonArray& result = this->parseCommand(buffer);
        
          result.printTo(Serial);

        buffer[0]=0;
        bufferSize=0;
      }
    }
  }
}
void MyTcpServerThread::listenEthernet()
{
  //Serial.println("Start Server to listen clients...");
  EthernetServer server = EthernetServer(8080);
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

        //TODO: Test - this executes actual commands and blocks the thread until done
        JsonArray& result = this->parseCommand(receivedText);
        
          result.printTo(client);
          result.printTo(Serial);
        
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
JsonArray& MyTcpServerThread::parseCommand(String plainJson)
{
  // [
  //   {"=3":0.25,"@":2}, // for 2 ms and revert
  //   {"=13":0},//set indefinite
  //   {"!":20} // wait for 20 ms
  //   {"get/13":"?"}
  // ]
  // [{"=3":1,"@":2},{"?13":0},{"=13":0},{"?13":0},{"!":20}]
  // [set 3=1 for 2, get 13, set 13 to 0, get 13, wait 20]
    const int capacity = JSON_ARRAY_SIZE(10) + 10*JSON_OBJECT_SIZE(2) + 100;
    StaticJsonBuffer<capacity> jb;

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
                
                obj[">"] = 1;
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
                
                obj[">"] = 1;
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
              obj[">"] = 1;
            }
          }
         
        }
      }

      return arr;

    } else {
      // parseObject() failed
      JsonArray& arr = jb.createArray();
      arr.add("Error parsing message");
      Logger::debugln("Deserialize received message failed.");
      return arr;
    }
}
int MyTcpServerThread::getPin(const byte startIndex, const char* key)
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
