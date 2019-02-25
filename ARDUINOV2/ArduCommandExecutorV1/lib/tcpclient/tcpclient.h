#ifndef tcpclient_h
#define tcpclient_h

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetClient.h>

class MyMonitorTcpClientThread: public Thread
{
  public: boolean ConnectToServer(EthernetClient arduinoClient, const byte* ip, const int port);
  
  private: void run();
};
#endif