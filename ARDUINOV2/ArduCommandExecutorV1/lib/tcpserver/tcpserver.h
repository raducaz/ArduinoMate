#ifndef tcpserver_h
#define tcpserver_h

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetServer.h>

class MyTcpServerThread: public Thread
{
    public: MyTcpServerThread();
    ~MyTcpServerThread();
    public: void run();
            void processCommand(const char* commandText, EthernetClient& client);
  
};
#endif