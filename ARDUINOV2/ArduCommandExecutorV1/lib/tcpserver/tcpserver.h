#ifndef tcpserver_h
#define tcpserver_h

#include <Arduino.h>
#include <ArduinoJson.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetServer.h>

class MyTcpServerThread: public Thread
{
        static const unsigned int MAXBUFFERSIZE = 200;
        char buffer[MAXBUFFERSIZE] = ""; 
        unsigned int bufferSize = 0;

    public: MyTcpServerThread();
    ~MyTcpServerThread();
    public: void run();
            void processCommand(const char* commandText, EthernetClient& client);

    private: void listenSerial();
             void listenEthernet();
             void parseCommand(String plainJson);
             int getPin(const byte size, const char* key);
  
};
#endif