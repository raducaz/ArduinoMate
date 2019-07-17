#include <globals.h>

#ifdef GEN     
    //--------DEVICE SPECIFIC GENERATOR---------------------------
    byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x72 };
    const byte ip[] = { 192, 168, 1, 100 }; //This needs to match the name configured on Android App
    int arduinoPort = 8080; //This needs to match the name configured on Android App
    char arduinoName[] = "Generator"; //This needs to match the name configured on Android App
    //--------DEVICE SPECIFIC---------------------------
    byte serverIp[] = { 192, 168, 1, 12 }; // Android device IP
    byte gateway[] = { 192, 168, 1, 1 };
    byte dns[] = { 192, 168, 1, 1 };

    const byte ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
    const byte ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!
    const byte ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
    const byte ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -
    const byte ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE
    const byte PresostatProbeSender = A3;
    const byte PresostatProbeReceiver = A4;
    const byte CurrentSensor = A1; //A1 is used by SD card ca drop value some times

#endif

#ifdef TAP     
    //--------DEVICE SPECIFIC GENERATOR---------------------------
    byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x71 };
    const byte ip[] = { 192, 168, 1, 101 }; //This needs to match the name configured on Android App
    int arduinoPort = 8081; //This needs to match the name configured on Android App
    char arduinoName[] = "TAP"; //This needs to match the name configured on Android App
    //--------DEVICE SPECIFIC---------------------------
    byte serverIp[] = { 192, 168, 1, 12 }; // Android device IP
    byte gateway[] = { 192, 168, 1, 1 };
    byte dns[] = { 192, 168, 1, 1 };

    const byte Priza8 = 8; 
    const byte Priza7 = 7; 
    const byte Priza6 = 6; 
    const byte Priza5 = 5; // Not yet connected
    const byte Priza4 = 3; 
    const byte Priza3 = 2; 
    const byte TapProbeSender = A3;
    const byte TapProbeReceiver = A4;
#endif
#ifdef BOIL     
    //--------DEVICE SPECIFIC GENERATOR---------------------------
    byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x70 };
    const byte ip[] = { 192, 168, 1, 102 }; //This needs to match the name configured on Android App
    int arduinoPort = 8082; //This needs to match the name configured on Android App
    char arduinoName[] = "TAP"; //This needs to match the name configured on Android App
    //--------DEVICE SPECIFIC---------------------------
    byte serverIp[] = { 192, 168, 1, 12 }; // Android device IP
    byte gateway[] = { 192, 168, 1, 1 };
    byte dns[] = { 192, 168, 1, 1 };

    const byte PrizaDreapta = 3; // controleaza releul pentru priza dreapta (cu senzor de curent) 
    const byte PrizaStanga = 5; // controleaza releul pentru priza stanga (fara senzor de curent) 
    const byte CurrentSensor = A1;
#endif
#ifdef TEST     
    //--------DEVICE SPECIFIC GENERATOR---------------------------
    byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x72 };
    const byte ip[] = { 192, 168, 100, 100 }; //This needs to match the name configured on Android App
    int arduinoPort = 8080; //This needs to match the name configured on Android App
    char arduinoName[] = "Generator"; //This needs to match the name configured on Android App
    //--------DEVICE SPECIFIC---------------------------
    byte serverIp[] = { 192, 168, 100, 12 }; // Android device IP
    byte gateway[] = { 192, 168, 100, 1 };
    byte dns[] = { 192, 168, 100, 1 };

    const byte ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
    const byte ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!
    const byte ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
    const byte ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -
    const byte ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE
    const byte PresostatProbeSender = A3;
    const byte PresostatProbeReceiver = A4;
    const byte CurrentSensor = A1; //A1 is used by SD card ca drop value some times

#endif

// COMMON
int serverPort = 9090; //This is default port for Android server
byte subnet[] = { 255, 255, 255, 0 };
const unsigned int MAXBUFFERSIZE = 250; //for input and output - same size
const byte WatchDog = 9;
const byte SDCard = 4;
#ifdef HASTEMP
    const byte TemperatureSensor = A2;
#endif
// COMMON

bool isDebug()
{
  return true;
}
bool useEthernet()
{
  return false;
}
void setupPins()
{
    pinMode(SDCard, OUTPUT);
    pinMode(WatchDog, OUTPUT);
    #ifdef HASTEMP
        pinMode(TemperatureSensor, INPUT);
    #endif
    #ifdef GEN 
        pinMode(ContactGenerator, OUTPUT);
        pinMode(ActuatorNormal, OUTPUT);
        pinMode(ActuatorInversat, OUTPUT);
        pinMode(ContactRetea220V, OUTPUT);
        pinMode(ContactDemaror12V, OUTPUT);
        pinMode(PresostatProbeSender, OUTPUT);
        pinMode(PresostatProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
        pinMode(CurrentSensor, INPUT);
    #endif
    #ifdef TAP
        pinMode(Priza8, OUTPUT);
        pinMode(Priza7, OUTPUT);
        pinMode(Priza6, OUTPUT);
        pinMode(Priza5, OUTPUT);
        pinMode(Priza4, OUTPUT);
        pinMode(Priza3, OUTPUT);
        pinMode(TapProbeSender, OUTPUT);
        pinMode(TapProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
    #endif    
    #ifdef BOIL
        pinMode(PrizaDreapta, OUTPUT);
        pinMode(PrizaStanga, OUTPUT);
        pinMode(CurrentSensor, INPUT);
    #endif
    #ifdef TEST 
        pinMode(ContactGenerator, OUTPUT);
        pinMode(ActuatorNormal, OUTPUT);
        pinMode(ActuatorInversat, OUTPUT);
        pinMode(ContactRetea220V, OUTPUT);
        pinMode(ContactDemaror12V, OUTPUT);
        pinMode(PresostatProbeSender, OUTPUT);
        pinMode(PresostatProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
        pinMode(CurrentSensor, INPUT);
    #endif
}
void initializePins()
{
    digitalWrite(SDCard, HIGH); // Disable SD Card 
    digitalWrite(WatchDog, HIGH); // Start with High 
    #ifdef GEN
        digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
        digitalWrite(ActuatorNormal, HIGH); // Decuplat 
        digitalWrite(ActuatorInversat, HIGH); // Decuplat
        digitalWrite(ContactRetea220V, HIGH); // Decuplat
        digitalWrite(ContactDemaror12V, LOW); // Decuplat
        digitalWrite(PresostatProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
    #endif
    #ifdef TAP
        digitalWrite(Priza8, HIGH); 
        digitalWrite(Priza7, HIGH); 
        digitalWrite(Priza6, HIGH); 
        digitalWrite(Priza5, HIGH); 
        digitalWrite(Priza3, HIGH); // decuplat
        digitalWrite(Priza4, HIGH); 
        digitalWrite(TapProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
    #endif
    #ifdef BOIL
        digitalWrite(PrizaDreapta, HIGH); // Decuplat
        digitalWrite(PrizaStanga, HIGH); // Decuplat 
    #endif
}
