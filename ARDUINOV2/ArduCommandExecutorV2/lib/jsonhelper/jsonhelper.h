// #ifndef jsonhelper_h
// #define jsonhelper_h

// #include <Arduino.h>
// #include <Thread.h>
// #include <ArduinoJson.h>
// #include <ACS712.h>

// class JSONSerializer
// {
//     ACS712 sensor(ACS712_30A, A1);

//     public: static JsonObject& constructPinStatesJSON(const byte* deviceIp,
//                                     const byte deviceState,
//                                     byte pinType, 
//                                     int* pinStates, byte size);
//     static JsonObject& constructPinStatesJSON(const byte* deviceIp,
//                                     const byte deviceState,
//                                     byte pinType, 
//                                     const int* pinStates, byte size, 
//                                     const char* msg);
//     static JsonObject& constructDeviceJSON(
//                                     const char* deviceName,
//                                     const byte deviceState);
// };