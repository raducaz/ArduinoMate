#include <utils.h>
#include <Arduino.h>
#include <math.h>
#include "Log.h"

int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin)
{
  int cmdLen = strlen(cmd);
  
  byte i = 1; //First char is cmd char followed by first argument =3:1:12
  byte index = 0;
  char res[cmdLen] = "";
  const char separator = ':';
  while(cmd[i]>0){

    if(index>paramIndex)
        break;

    if(cmd[i]==separator){
      index++;

    } else if(index==paramIndex){
      size_t len = strlen(res);
      res[len] = cmd[i];
      res[len + 1] = '\0';
    }

    i++;
  }

  int iRes = returnAsPin ? getPin(res) : atoi(res);
  // Log::debugln(F("iRes:"),iRes);
  char buffer[6]=""; //can be !2000\0
  char* sRes = itoa(iRes,buffer,10);
  // Log::debugln(F("sRes:"),sRes);
  // Log::debugln(F("res:"),res);

  if(iRes==0 && strcmp(res,"0")!=0){
      return -1;
  }
  else{
    return iRes;
  }
    
  
}