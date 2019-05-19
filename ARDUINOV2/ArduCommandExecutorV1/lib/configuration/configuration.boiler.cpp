// #include "configuration.h"
// #include <Arduino.h>

// //--------DEVICE SPECIFIC---------------------------

// const int PrizaDreapta = 3; // controleaza releul pentru priza dreapta (cu senzor de curent) 
// const int PrizaStanga = 5; // controleaza releul pentru priza stanga (fara senzor de curent) 
// const int CurrentSensor = A1;

// bool Configuration::isDebug()
// {
//   return true;
// }
// bool Configuration::useEthernet()
// {
//   return true;
// }
// void Configuration::setupPins()
// {
//   // OUTPUT PINS
//   pinMode(PrizaDreapta, OUTPUT);
//   pinMode(PrizaStanga, OUTPUT);
//   pinMode(CurrentSensor, INPUT);
// }
// void Configuration::initializePins()
// {  
//   digitalWrite(PrizaDreapta, HIGH); // Decuplat
//   digitalWrite(PrizaStanga, HIGH); // Decuplat 
// }

// //--------DEVICE SPECIFIC---------------------------

// // const byte serverIp[] = { 192, 168, 100, 3 };
// // public: byte* getServerIp() const
// // {  
// //   return serverIp;
// // }
// // public:static int getServerPort()
// // {  
// //   return 9090;
// // }
// // public:static byte* getMac()
// // {  
// //   return { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x77 };
// // }
// // public:static byte* getIp()
// // {  
// //   return { 192, 168, 100, 100 };
// // }
// // public:static byte* getGateway()
// // {  
// //   return { 192, 168, 100, 1 };
// // }
// // public:static byte* getDns()
// // {  
// //   return { 192, 168, 100, 1 };
// // }
// // public:static byte* getSubnet()
// // {  
// //   return { 255, 255, 255, 0 };
// // }
