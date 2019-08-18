#include <Arduino.h>

#include <math.h>

#include "log.h"



int getPin(const char* key);
int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin);