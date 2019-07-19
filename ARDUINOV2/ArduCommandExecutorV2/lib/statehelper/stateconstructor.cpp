#include <stateconstructor.h>

const char PIN[4] = "pin";
const char NAME[5] = "name";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char MSG[4] = "msg";
const char DIGITAL[12] = "digitalPins";
const char ANALOG[11] = "analogPins";

void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    const int* pinStates, byte size, 
    const char* msg,
    EthernetClient arduinoClient)
  {
      StaticJsonBuffer<300> _buffer;
      
      Log::debugln(F("FreeMem:"), freeMemory());

      JsonObject& _root = _buffer.createObject();
      if(strcmp(msg,"") != 0)
          _root[MSG] = msg;
      _root[NAME] = deviceName;
      _root[DEVICESTATE] = deviceState;

      Log::debugln(F("FreeMem:"), freeMemory());

      JsonArray& psArr = _root.createNestedArray(pinType==0?DIGITAL:ANALOG);
      for(byte i=0;i<size;i++)
      {
          psArr.add(pinStates[i]);
      }

      Log::debugln(F("FreeMem:"), freeMemory());

      _root.printTo(arduinoClient);
      arduinoClient.println();

      Log::debugln(F("FreeMem:"), freeMemory());

    //   _root.printTo(Serial);
    //   Serial.println();

      Log::debugln(F("FreeMem:"), freeMemory());
  }
void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    int* pinStates, byte size, EthernetClient arduinoClient)
{
    constructPinStatesJSON(deviceName, deviceState, pinType, pinStates, size, "", arduinoClient);
}