#ifndef jsonhelper_h
#define jsonhelper_h

#include <Arduino.h>
#include <Thread.h>
#include <ArduinoJson.h>

class JSONSerializer
{
    public: static JsonObject& constructPinStatesJSON();
     static JsonObject& constructPinStatesJSON(const char* msg);
     static JsonObject& constructPinStateJSON(byte pin, const char* msg);
     static JsonObject& constructPinStateJSON(byte pin);
     static JsonObject& constructFctStateJSON(byte state, const char* msg, const char* fct);
};