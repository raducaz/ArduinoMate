#include "configuration.h"
#include <Arduino.h>

//--------DEVICE SPECIFIC TAP---------------------------

const int Priza8 = 8; 
const int Priza7 = 7; 
const int Priza6 = 6; 
const int Priza5 = 5; // Not yet connected

const int TapProbeSender = A3;
const int TapProbeReceiver = A4;

bool Configuration::isDebug()
{
  return true;
}
bool Configuration::useEthernet()
{
  return true;
}
void Configuration::setupPins()
{
  // OUTPUT PINS
  pinMode(Priza8, OUTPUT);
  pinMode(Priza7, OUTPUT);
  pinMode(Priza6, OUTPUT);
  pinMode(Priza5, OUTPUT);

  pinMode(TapProbeSender, OUTPUT);
  pinMode(TapProbeReceiver, INPUT_PULLUP); //Sets it to HIGH

}
void Configuration::initializePins()
{  
  digitalWrite(Priza8, HIGH); 
  digitalWrite(Priza7, HIGH); 
  digitalWrite(Priza6, HIGH); 
  digitalWrite(Priza5, HIGH); 

  digitalWrite(TapProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
}

//--------DEVICE SPECIFIC---------------------------

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
