#include <utils.h>
#include <Arduino.h>
#include <math.h>
#include "log.h"

int getPin(const char* key)
{
  char sPin[4]="";
  bool isAnalogPin = (key[0]=='A'||key[0]=='a');
  
  byte i= 0; 
  byte index = (isAnalogPin ? 1 : 0);
  while(key[index+i]){
    sPin[i]=key[index+i];
    i++;
  }
  sPin[i] = '\0';
  
  int pinNo = atoi(sPin);
  //return isAnalogPin ? (pinNo<2 ? pinNo+16 : pinNo+18) : pinNo;
  return isAnalogPin ? pinNo+14 : pinNo; //A0 is 14, A1 is 15...
}