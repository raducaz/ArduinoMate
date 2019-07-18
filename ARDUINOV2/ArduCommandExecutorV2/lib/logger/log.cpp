#include "log.h"

#include <Arduino.h>
#include <Ethernet.h>
#include <globals.h>

void Log::debugln(const __FlashStringHelper* lbl, const char* msg)
{
    if(isDebug())
    {
      Serial.print(lbl);Serial.println(msg);
    }
}
void Log::debugln(const __FlashStringHelper* lbl, byte msg)
{
    if(isDebug())
    {
      Serial.print(lbl);Serial.println(msg);
    }
}
void Log::debugln(const __FlashStringHelper* lbl, int msg)
{
    if(isDebug())
    {
      Serial.print(lbl);Serial.println(msg);
    }
}
void Log::debugln(const __FlashStringHelper* lbl, unsigned int msg)
{
    if(isDebug())
    {
      Serial.print(lbl);Serial.println(msg);
    }
}
void Log::debugln(const __FlashStringHelper* lbl, IPAddress ip)
{
    if(isDebug())
    {
      Serial.print(lbl);Serial.println(ip);
    }
}
void Log::debugln(const char* msg)
{
    if(isDebug())
      Log::logln(msg);
}
void Log::debugln(const __FlashStringHelper* msg)
{
    if(isDebug())
      Serial.println(msg);
}
void Log::debugln(const int msg)
{
    if(isDebug())
      Log::logln(msg);
}
void Log::debugln(IPAddress ip)
{
    if(isDebug())
      Log::logln(ip);
}
void Log::debug(const char* msg)
{
    if(isDebug())
      Log::log(msg);
}
void Log::debug(const int msg)
{
    if(isDebug())
      Log::logln(msg);
}
void Log::debugbyte(const byte* msg)
{
    if(isDebug())
      Log::logbyteln(msg);
}
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
  