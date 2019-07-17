
#ifndef getCommandParam_h
#define getCommandParam_h

#include <Arduino.h>
#include <math.h>
#include "Log.h"
#include <MemoryFree.h>

int getPin(const char* key);
int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin);

#endif