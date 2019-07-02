#ifndef configuration_h
#define configuration_h

#include <Arduino.h>
#include <Ethernet.h>

class Configuration
{
    

    public:
        static const byte WatchDog = 9;
        static bool isDebug();
        static bool useEthernet();
        static void setupPins();
        static void initializePins();
    
};
#endif