// #include "configuration.h"
// #include <Arduino.h>

// //--------DEVICE SPECIFIC TAP---------------------------

// const byte Priza8 = 8; 
// const byte Priza7 = 7; 
// const byte Priza6 = 6; 
// const byte Priza5 = 5; // Not yet connected

// const byte SDCard = 4;

// const byte Priza4 = 3; 
// const byte Priza3 = 2; 

// const byte TapProbeSender = A3;
// const byte TapProbeReceiver = A4;

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
//     // OUTPUT PINS
//     pinMode(SDCard, OUTPUT);
//     pinMode(Priza8, OUTPUT);
//     pinMode(Priza7, OUTPUT);
//     pinMode(Priza6, OUTPUT);
//     pinMode(Priza5, OUTPUT);
//     pinMode(Priza4, OUTPUT);
//     pinMode(Priza3, OUTPUT);


//     pinMode(TapProbeSender, OUTPUT);
//     pinMode(TapProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
    
//     pinMode(TemperatureSensor, INPUT);
//     pinMode(Configuration::WatchDog, OUTPUT);
// }
// void Configuration::initializePins()
// {  
//     digitalWrite(SDCard, HIGH); // Disable SD Card 
//     digitalWrite(Priza8, HIGH); 
//     digitalWrite(Priza7, HIGH); 
//     digitalWrite(Priza6, HIGH); 
//     digitalWrite(Priza5, HIGH); 
//     digitalWrite(Priza3, HIGH); // decuplat
//     digitalWrite(Priza4, HIGH); 

//     digitalWrite(TapProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
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
