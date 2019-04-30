#include "executor.h"

#include <Arduino.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <ArduinoJson.h>
#include "logger.h"

void MyExecutor::wait(unsigned int msInterval)
{
    Logger::debug("wait for ");Logger::debug(msInterval);Logger::debugln(" ms");
    unsigned long waitStart = millis();

    unsigned long current = millis();
    while((current - waitStart)< msInterval) 
    {
      current = millis();
    }; 
}  

void MyExecutor::sendToServer(const char* msg, EthernetClient& client)
{
  client.println(msg);

  Logger::debugln(msg);
}
void MyExecutor::sendToServer(JsonObject& json, EthernetClient& client)
{
  json.printTo(client);
  client.println();

  json.printTo(Serial);
  Serial.println();
}
void MyExecutor::setPin(byte pin, byte state)
{
  Logger::debug("setPin:"); Logger::debug(pin);Logger::debug(" to ");Logger::debugln(state);
  digitalWrite(pin, state);
}
void MyExecutor::setPinTemp(byte pin, byte state, unsigned int interval)
{
  // if(client.connected())
  // {
  //   //sendToServer(JSONSerializer::constructPinStateJSON(pin), client);
  // }
  Logger::debug("setPinTemp:");Logger::debug(pin);Logger::debug(" to ");Logger::debug(state);
  Logger::debug(" for ");Logger::debugln(interval);

  byte state1 = digitalRead(pin);
  digitalWrite(pin, state);
  wait(interval);
  digitalWrite(pin, state1);
}
