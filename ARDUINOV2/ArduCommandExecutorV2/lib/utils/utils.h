#include <Arduino.h>

#include <math.h>

#include "Log.h"



int getPin(const char* key);
int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin);