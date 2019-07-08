#include <Arduino.h>
#include <ArduinoJson.h>

const char PIN[4] = "pin";
const char NAME[5] = "name";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char MSG[4] = "msg";
const char DIGITAL[12] = "digitalPins";
const char ANALOG[11] = "analogPins";

class JSONSerializer
{
    public: static JsonObject& constructPinStatesJSON(
                                    const char* deviceName,
                                    const byte deviceState,
                                    byte pinType, 
                                    float* pinStates, byte size)
    {
        return constructPinStatesJSON(deviceName, deviceState, pinType, pinStates, size, "");
    }
    public: static JsonObject& constructPinStatesJSON(
                                    const char* deviceName,
                                    const byte deviceState,
                                    byte pinType, 
                                    const float* pinStates, byte size, 
                                    const char* msg)
    {
        StaticJsonBuffer<400> _buffer;
        JsonObject& _root = _buffer.createObject();
        if(strcmp(msg,"") != 0)
            _root[MSG] = msg;
        _root[NAME] = deviceName;
        _root[DEVICESTATE] = deviceState;

        JsonArray& psArr = _root.createNestedArray(pinType==0?DIGITAL:ANALOG);
        for(byte i=0;i<size;i++)
        {
            psArr.add(pinStates[i]);
        }

        return _root;
    }
    // static String ipToString(const byte* ip)
    // {
    //     char[20] result;
    //     char sIp[4] = "";
    //     for(byte i=0;i<4;i++){
    //         sprintf (sIp, "%d", ip[i]);
    //         result[i] = sIp[i];
    //     }

    //     return result;
    // }
    // public: static JsonObject& constructPinStateJSON(byte pin, const char* msg)
    // {
    //     StaticJsonBuffer<400> _buffer;
    //     JsonObject& _root = _buffer.createObject();
    //     if(strcmp(msg,"")==0)
    //         _root[MSG] = msg;
    //     _root[PIN] = pin;
    //     _root[IP] = "192.168.100.100";
    //     _root[DEVICESTATE] = 0;
    //     _root[VALUE] = digitalRead(pin);
        
    //     return _root;
    // }
    // public: static JsonObject& constructPinStateJSON(byte pin)
    // {
    //     return constructPinStateJSON(pin, "");
    // }
    // public: static JsonObject& constructFctStateJSON(byte state, const char* msg, const char* fct)
    // {
    //     StaticJsonBuffer<400> _buffer;
    //     JsonObject& _root = _buffer.createObject();
    //     _root[MSG] = msg;
    //     _root[IP] = "192.168.100.100";
    //     _root[DEVICESTATE] = 0;
    //     _root[FCTNAME] = fct;
    //     _root[FCTSTATE] = state;
        
    //     return _root;
    // }
};