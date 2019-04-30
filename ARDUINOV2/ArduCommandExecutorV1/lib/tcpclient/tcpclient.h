#ifndef tcpclient_h
#define tcpclient_h

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetClient.h>

class MyMonitorTcpClientThread: public Thread
{
  EthernetClient arduinoClient;

  byte* mac;
  byte* ip;

  byte* serverIp;
  int serverPort;
  byte* gateway;
  byte* dns;
  byte* subnet;

  public: MyMonitorTcpClientThread(
    byte* ip, 
                                  byte* mac, 
                                  byte* serverIp, 
                                  int serverPort, 
                                  byte* gateway, byte* dns, byte* subnet
                                  );

  public: boolean ConnectToServer(const byte* ip, const int port);
  
  private: void run();
};
#endif