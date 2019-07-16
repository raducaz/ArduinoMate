#include "configuration.h"
#include <Arduino.h>

//--------DEVICE SPECIFIC---------------------------

const byte ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const byte ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!
const byte SDCard = 4;

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const byte ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const byte ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

const byte ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

const byte PresostatProbeSender = A3;
const byte PresostatProbeReceiver = A4;
const byte CurrentSensor = A1; //A1 is used by SD card ca drop value some times

bool Configuration::isDebug()
{
  return true;
}
bool Configuration::useEthernet()
{
  return false;
}
void Configuration::setupPins()
{
  // OUTPUT PINS
  pinMode(SDCard, OUTPUT);
  pinMode(ContactGenerator, OUTPUT);
  pinMode(ActuatorNormal, OUTPUT);
  pinMode(ActuatorInversat, OUTPUT);
  pinMode(ContactRetea220V, OUTPUT);
  pinMode(ContactDemaror12V, OUTPUT);
  pinMode(PresostatProbeSender, OUTPUT);
  pinMode(PresostatProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
  pinMode(CurrentSensor, INPUT);
  pinMode(TemperatureSensor, INPUT);

  pinMode(WatchDog, OUTPUT);
}
void Configuration::initializePins()
{
  digitalWrite(SDCard, HIGH); // Disable SD Card 
  digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
  digitalWrite(ActuatorNormal, HIGH); // Decuplat 
  digitalWrite(ActuatorInversat, HIGH); // Decuplat
  digitalWrite(ContactRetea220V, HIGH); // Decuplat
  digitalWrite(ContactDemaror12V, LOW); // Decuplat
  //digitalWrite(WatchDog, LOW); // Initial state
  digitalWrite(PresostatProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
}

