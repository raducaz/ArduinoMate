#ifndef log_h
#define log_h

#include <Arduino.h>
#include <Ethernet.h>

class Log
{
    public:
        static void debugln(const __FlashStringHelper* msg);
        static void debugln(const __FlashStringHelper* lbl, const char* msg);
        static void debugln(const __FlashStringHelper* lbl, byte msg);
        static void debugln(const __FlashStringHelper* lbl, int msg);
        static void debugln(const __FlashStringHelper* lbl, unsigned int msg);
        static void debugln(const __FlashStringHelper* lbl, IPAddress ip);
        static void debug(const __FlashStringHelper* msg);
        static void debug(const char* msg);
        static void debugln(const int msg);
        static void debugln(IPAddress ip);
        static void debugln(const char* msg);
        static void debug(const int msg);
        static void debugbyte(const byte* msg);
        
    private:
        static void logln(IPAddress ip);
        static void logln(const char* msg);
        static void log(const char* msg);
        static void logln(const int msg);
        static void logInt(const int msg);
        static void logbyteln(const byte* msg);

};
#endif