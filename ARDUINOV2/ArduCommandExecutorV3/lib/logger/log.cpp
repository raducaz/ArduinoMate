#include "log.h"

#include <Arduino.h>
#include <Ethernet.h>
#include <globals.h>

void Log::debugln(const __FlashStringHelper* lbl, const char* msg)
{
    #ifdef DEBUG
      Serial.print(lbl);Serial.println(msg);
    #endif
}
void Log::debugln(const __FlashStringHelper* lbl, byte msg)
{
    #ifdef DEBUG
      Serial.print(lbl);Serial.println(msg);
    #endif
}
void Log::debugln(const __FlashStringHelper* lbl, int msg)
{
    #ifdef DEBUG
      Serial.print(lbl);Serial.println(msg);
    #endif
}
void Log::debugln(const __FlashStringHelper* lbl, unsigned int msg)
{
    #ifdef DEBUG
      Serial.print(lbl);Serial.println(msg);
    #endif
}
void Log::debugln(const __FlashStringHelper* lbl, IPAddress ip)
{
    #ifdef DEBUG
      Serial.print(lbl);Serial.println(ip);
    #endif
}
void Log::debugln(const char* msg)
{
    #ifdef DEBUG
      Log::logln(msg);
    #endif
}
void Log::debugln(const __FlashStringHelper* msg)
{
    #ifdef DEBUG
      Serial.println(msg);
    #endif
}
void Log::debugln(const int msg)
{
    #ifdef DEBUG
      Log::logln(msg);
    #endif
}
void Log::debugln(IPAddress ip)
{
    #ifdef DEBUG
      Log::logln(ip);
    #endif
}
void Log::debug(const char* msg)
{
    #ifdef DEBUG
      Log::log(msg);
    #endif
}
void Log::debug(const int msg)
{
    #ifdef DEBUG
      Log::logln(msg);
    #endif
}
void Log::debugbyte(const byte* msg)
{
    #ifdef DEBUG
      Log::logbyteln(msg);
    #endif
}


// Private methods
void Log::logln(IPAddress ip)
{
    Serial.println(ip);
}
void Log::logln(const char* msg)
{
    Serial.println(msg);
}
void Log::log(const char* msg)
{
    Serial.print(msg);
}
void Log::logln(const int msg)
{
    Serial.println(msg, DEC);
}
void Log::logInt(const int msg)
{
    Serial.print(msg, DEC);
}
void Log::logbyteln(const byte* msg)
{
    for(byte i=0;i<4;i++){
      Serial.print(msg[i]); 
      
      if(i<3) Serial.print(F(",")); 
    }
}
  