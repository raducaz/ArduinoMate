#include <Arduino.h>
#include <ArduinoJson.h>

const char PIN[4] = "pin";
const char IP[3] = "ip";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char MSG[4] = "msg";
const char PINSTATES[10] = "pinStates";

class JSONSerializer
{
    public: static JsonObject& constructPinStatesJSON()
    {
        // return constructPinStatesJSON("");

        StaticJsonBuffer<400> _buffer;
        JsonObject& _root = _buffer.createObject();
        
        _root[IP] = "192.168.100.100";
        _root[DEVICESTATE] = 0;//DeviceState;
        JsonArray& pinStates = _root.createNestedArray(PINSTATES);
        for(byte i=0;i<10;i++)
        {
            pinStates.add(digitalRead(i));
        }
        
        return _root;

    }
    public: static JsonObject& constructPinStatesJSON(const char* msg)
    {
        StaticJsonBuffer<400> _buffer;
        JsonObject& _root = _buffer.createObject();
        if(strcmp(msg,"") != 0)
            _root[MSG] = msg;
        _root[IP] = "192.168.100.100";
        _root[DEVICESTATE] = 0;//DeviceState;
        JsonArray& pinStates = _root.createNestedArray(PINSTATES);
        for(byte i=0;i<10;i++)
        {
            pinStates.add(digitalRead(i));
        }
        
        return _root;
    }
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