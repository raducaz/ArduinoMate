#ifndef executor_h
#define executor_h

#include <Arduino.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <ArduinoJson.h>

class MyExecutor
{
    public:
        static void wait(unsigned int msInterval);
        static void sendToServer(const char* msg, EthernetClient& client);
        static void sendToServer(JsonObject& json, EthernetClient& client);
        static void setDigitalPin(byte pin, byte state);
        static void setDigitalPinTemp(byte pin, byte state, unsigned int interval);
        static void setAnalogPin(byte pin, float state);
        static void setAnalogPinTemp(byte pin, float state, unsigned int interval);

};
#endif