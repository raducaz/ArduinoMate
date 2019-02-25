#include <Arduino.h>

class Configuration
{
  public:static void setupPins()
  {
    // OUTPUT PINS
    pinMode(3, OUTPUT);
  }
  public:static void initializePins()
  {  
    digitalWrite(3, HIGH);
  }

  // const byte serverIp[] = { 192, 168, 100, 3 };
  // public: byte* getServerIp() const
  // {  
  //   return serverIp;
  // }
  // public:static int getServerPort()
  // {  
  //   return 9090;
  // }
  // public:static byte* getMac()
  // {  
  //   return { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x77 };
  // }
  // public:static byte* getIp()
  // {  
  //   return { 192, 168, 100, 100 };
  // }
  // public:static byte* getGateway()
  // {  
  //   return { 192, 168, 100, 1 };
  // }
  // public:static byte* getDns()
  // {  
  //   return { 192, 168, 100, 1 };
  // }
  // public:static byte* getSubnet()
  // {  
  //   return { 255, 255, 255, 0 };
  // }
};