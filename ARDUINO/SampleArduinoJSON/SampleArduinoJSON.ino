#include <ArduinoJson.h>
#include <SPI.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif
static const int pinBufferSize = 2*JSON_ARRAY_SIZE(13) + 14*JSON_OBJECT_SIZE(1);
static const int fctBufferSize = 2*JSON_ARRAY_SIZE(13) + 14*JSON_OBJECT_SIZE(1);
class PinsJsonBuilder
{
  StaticJsonBuffer _buffer;
    public: JsonObject& _root;

public:
    PinsJsonBuilder(StaticJsonBuffer buf, int noStates)
        : _root(buf.createObject())
    {
      _buffer = buf;
      _root["ip"] = "192.168.11.100";
      _root["state"] = 1;
      addPinStates(noStates);
    }

    void addPinStates(int noStates)
    {
      JsonArray& states = _root.createNestedArray("p");

      for(byte i=1;i<=noStates;i++)
      {
        addPin(states, i);
      } 
    }
    void addPin(JsonArray& pinStates, byte i)
    {
        JsonObject& pinState = pinStates.createNestedObject();
        pinState[getPinName(i)] = digitalRead(i);
    }
    
    void dumpTo(String& dest) const
    {
        _root.printTo(dest);
    }

private:
    const char* getPinName(byte i)
    {
        char* key = (char*)_buffer.alloc(5);
        sprintf(key, "P%d", i);
        return key;
    }
};

void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(8, OUTPUT);
  
}

PinsJsonBuilder* fctJBuilder = new PinsJsonBuilder(3);
void loop() {

  int pin = 5;
  if(digitalRead(pin)==0)
  {
    digitalWrite(pin,HIGH);
    Serial.print(digitalRead(pin));Serial.println(" pin high");
  }
  else
  {
    digitalWrite(pin,LOW);
    Serial.print(digitalRead(pin));Serial.println(" pin low");
  }

  StaticJsonBuffer<pinBufferSize> _buffer;
  String jsonMsg;
  PinsJsonBuilder* pinsJBuilder = new PinsJsonBuilder(_buffer, 13);
  pinsJBuilder->dumpTo(jsonMsg);
  
  delete pinsJBuilder;
  Serial.println(jsonMsg);

//  // Functions demo
//  if(fctJBuilder)
//  {
//    if(fctJBuilder->_root["p"][0]==0)
//      fctJBuilder->_root["p"][0] = 1;
//    else
//      fctJBuilder->_root["p"][0] = 0;
//      
//    fctJBuilder->dumpTo(jsonMsg);
//  }
//  
//  Serial.println(jsonMsg);

  delay(2000);
}

