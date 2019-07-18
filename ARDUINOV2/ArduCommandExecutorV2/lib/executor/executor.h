#ifndef executor_h
#define executor_h

#include <Arduino.h>
#include "log.h"

void wait(unsigned int msInterval);
void setDigitalPin(byte pin, byte state);
void setAnalogPin(byte pin, int state);
void setDigitalPinTemp(byte pin, byte state, unsigned int interval);
void setAnalogPinTemp(byte pin, int state, unsigned int interval);

#endif