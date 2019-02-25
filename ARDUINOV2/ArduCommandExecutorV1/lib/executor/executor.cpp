#include "executor.h"

#include <Arduino.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <ArduinoJson.h>


void MyExecutor::wait(unsigned int msInterval)
{
    unsigned long waitStart = millis();
//      Serial.print("wait");Serial.println(msInterval);Serial.println(waitStart);

    unsigned long current = millis();
    while((current - waitStart)< msInterval) 
    {
      current = millis();
      //Serial.print("Current:");Serial.println(current);
    }; // wait until 
    
//      Serial.print("done wait");Serial.println(millis());
}  

void MyExecutor::sendToServer(const char* msg, EthernetClient& client)
{
  client.println(msg);

  Serial.println(msg);
}
void MyExecutor::sendToServer(JsonObject& json, EthernetClient& client)
{
  json.printTo(client);
  client.println();

  json.printTo(Serial);
  Serial.println();
}
void MyExecutor::setPin(byte pin, byte state, EthernetClient& client)
{
  //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.println(state);
  digitalWrite(pin, state);
}
void MyExecutor::setPinTemp(byte pin, byte state, unsigned int interval, EthernetClient& client)
{
  //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.print(state);Serial.print(" for ");Serial.print(interval);Serial.println(" ms");
  if(client.connected())
  {
    //sendToServer(JSONSerializer::constructPinStateJSON(pin), client);
  }
  
  byte state1 = digitalRead(pin);
  digitalWrite(pin, state);
  wait(interval);
  digitalWrite(pin, state1);
}
