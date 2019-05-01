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
void MyExecutor::setDigitalPin(byte pin, byte state)
{
  Logger::debug("setPin:"); Logger::debug(pin);Logger::debug(" to ");Logger::debugln(state);
  digitalWrite(pin, state);
}
void MyExecutor::setAnalogPin(byte pin, float state)
{
  Logger::debug("setPin:"); Logger::debug(pin);Logger::debug(" to ");Logger::debugln(state);
  analogWrite(pin, state);
}
void MyExecutor::setDigitalPinTemp(byte pin, byte state, unsigned int interval)
{
  Logger::debug("setPinTemp:");Logger::debug(pin);Logger::debug(" to ");Logger::debug(state);
  Logger::debug(" for ");Logger::debugln(interval);

  byte state1 = digitalRead(pin);
  digitalWrite(pin, state);
  wait(interval);
  digitalWrite(pin, state1);
}

void MyExecutor::setAnalogPinTemp(byte pin, float state, unsigned int interval)
{
  Logger::debug("setPinTemp:");Logger::debug(pin);Logger::debug(" to ");Logger::debug(state);
  Logger::debug(" for ");Logger::debugln(interval);

  float state1 = analogRead(pin);
  analogWrite(pin, state);
  wait(interval);
  analogWrite(pin, state1);
}
