
#ifndef appendCommand_h
#define appendCommand_h

#include <Arduino.h>
#include <math.h>
#include "log.h"
#include <MemoryFree.h>
#include <globals.h>

bool appendCmdResult(char* res, char* cmd, char* value);
bool appendCmdResult(char* res, char* cmd, int value);
bool appendCmdResult(char* res, char* cmd, float value);

#endif