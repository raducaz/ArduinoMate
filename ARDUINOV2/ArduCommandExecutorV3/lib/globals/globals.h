#ifndef globals_h
#define globals_h

#include <Arduino.h>

// #define DEBUG /*if defined, enables debug prints*/

#define GEN
#define HASTEMP
#define HASCURRENT

// if defined listen and uses ethernet shield
#define LISTENETHERNET

// #define TAP

//#define TEST /* define this to upload to testing device*/

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

#ifdef HASTEMP
    extern const byte TemperatureSensor;
#endif

void setupPins();
void initializePins();

#endif