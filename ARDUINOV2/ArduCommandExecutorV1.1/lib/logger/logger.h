#ifndef logger_h
#define logger_h

#include <Arduino.h>
#include <Ethernet.h>

class Logger
{
    public:
        static void debugln(const char* msg);
        static void debug(const char* msg);
        static void debugln(const int msg);
        static void debugln(IPAddress ip);
        static void debug(const int msg);
        static void debugbyte(const byte* msg);
        static void logln(IPAddress ip);
        static void logln(const char* msg);
        static void log(const char* msg);
        static void logln(const int msg);
        static void log(const int msg);
        static void logbyteln(const byte* msg);

};
#endif