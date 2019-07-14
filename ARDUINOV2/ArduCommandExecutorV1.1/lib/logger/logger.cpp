#include "logger.h"

#include <Arduino.h>
#include <Ethernet.h>
#include <configuration.h>

void Logger::debugln(const char* msg)
{
    if(Configuration::isDebug())
      Logger::logln(msg);
}
void Logger::debugln(const int msg)
{
    if(Configuration::isDebug())
      Logger::logln(msg);
}
void Logger::debugln(IPAddress ip)
{
    if(Configuration::isDebug())
      Logger::logln(ip);
}
void Logger::debug(const char* msg)
{
    if(Configuration::isDebug())
      Logger::log(msg);
}
void Logger::debug(const int msg)
{
    if(Configuration::isDebug())
      Logger::logln(msg);
}
void Logger::debugbyte(const byte* msg)
{
    if(Configuration::isDebug())
      Logger::logbyteln(msg);
}
void Logger::logln(IPAddress ip)
{
    Serial.println(ip);
}
void Logger::logln(const char* msg)
{
    Serial.println(msg);
}
void Logger::log(const char* msg)
{
    Serial.print(msg);
}
void Logger::logln(const int msg)
{
    Serial.println(msg);
}
void Logger::log(const int msg)
{
    Serial.print(msg);
}
void Logger::logbyteln(const byte* msg)
{
    for(byte i=0;i<4;i++){
      Serial.print(msg[i]); 
      
      if(i<3) Serial.print(","); 
    }
}
  