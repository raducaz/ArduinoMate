#include "configuration.h"
#include <Arduino.h>

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

const int PresostatProbeSender = A3;
const int PresostatProbeReceiver = A4;
const int CurrentSensor = A1;

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
  pinMode(ContactGenerator, OUTPUT);
  pinMode(ActuatorNormal, OUTPUT);
  pinMode(ActuatorInversat, OUTPUT);
  pinMode(ContactRetea220V, OUTPUT);
  pinMode(ContactDemaror12V, OUTPUT);
  pinMode(PresostatProbeSender, OUTPUT);
  pinMode(PresostatProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
  pinMode(CurrentSensor, INPUT);
}
void Configuration::initializePins()
{  
  digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
  digitalWrite(ActuatorNormal, HIGH); // Decuplat 
  digitalWrite(ActuatorInversat, HIGH); // Decuplat
  digitalWrite(ContactRetea220V, HIGH); // Decuplat
  digitalWrite(ContactDemaror12V, LOW); // Decuplat
  digitalWrite(PresostatProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
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
