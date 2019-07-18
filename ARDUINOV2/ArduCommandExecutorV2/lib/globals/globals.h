#ifndef globals_h
#define globals_h

#include <Arduino.h>

#define GEN
#define HASTEMP
#define HASCURRENT

//--------DEVICE SPECIFIC GENERATOR---------------------------
extern byte mac[];
extern const byte ip[]; //This needs to match the name configured on Android App
extern int arduinoPort; //This needs to match the name configured on Android App
extern char arduinoName[]; //This needs to match the name configured on Android App
//--------DEVICE SPECIFIC---------------------------

extern byte serverIp[]; // Android device IP
extern int serverPort; //This is default port for Android server
extern byte gateway[];
extern byte dns[];
extern byte subnet[];

extern const unsigned int MAXBUFFERSIZE; //for input and output - same size

extern const byte WatchDog;

#ifdef HASTEMP
    extern const byte TemperatureSensor;
#endif

bool isDebug();
bool useEthernet();
void setupPins();
void initializePins();

#endif