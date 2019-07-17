#ifndef stateconstructor_h
#define stateconstructor_h

#include <Arduino.h>
#include "Log.h"
#include <ArduinoJson.h>
#include <MemoryFree.h>
#include <globals.h>
#include <EthernetClient.h>

void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    const int* pinStates, byte size, 
    const char* msg, 
    EthernetClient arduinoClient);

void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    int* pinStates, byte size, EthernetClient arduinoClient);

#endif