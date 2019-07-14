#ifndef globals_h
#define globals_h

//--------DEVICE SPECIFIC GENERATOR---------------------------
byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x72 };
const byte ip[] = { 192, 168, 100, 100 }; //This needs to match the name configured on Android App
int arduinoPort = 8080; //This needs to match the name configured on Android App
char arduinoName[] = "Generator"; //This needs to match the name configured on Android App
//--------DEVICE SPECIFIC---------------------------

byte serverIp[] = { 192, 168, 100, 12 }; // Android device IP
int serverPort = 9090; //This is default port for Android server
byte gateway[] = { 192, 168, 100, 1 };
byte dns[] = { 192, 168, 100, 1 };
byte subnet[] = { 255, 255, 255, 0 };

#endif