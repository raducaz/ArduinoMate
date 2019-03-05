#include "tcpclient.h"

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <jsonhelper.cpp>
#include <executor.h>
#include "logger.h"

MyMonitorTcpClientThread::MyMonitorTcpClientThread(byte* ip, 
                                        byte* mac, 
                                        byte* serverIp, 
                                        int serverPort, 
                                        byte* gateway, byte* dns, byte* subnet):Thread()
{
  this->ip = ip;
  this->mac = mac;
  this->serverIp = serverIp;
  this->serverPort = serverPort;
  this-> gateway = gateway;
  this->dns = dns;
  this->subnet = subnet;
}
boolean MyMonitorTcpClientThread::ConnectToServer(const byte* ip, const int port)
{
  Logger::logln("Try connect to ");
  Logger::logbyteln(ip);

  if(arduinoClient)
  {
    arduinoClient.stop();
  }

  if (arduinoClient.connect(ip, port)) {    
    return arduinoClient.connected();
  }

  // Failed to connect
  Logger::debugln("MON: Client didn't connect to ");
  Logger::debugbyte(ip);
  
  return false;
}
  
// Function executed on thread execution
void MyMonitorTcpClientThread::run(){
  
  int i = 0;
  while(!(arduinoClient.connected()) && (i < 2))
  {
    ConnectToServer(serverIp, serverPort);
    i++;
  }
  if(!arduinoClient.connected())
  {
    Logger::debugln("MON: Check connection to gateway.");
    if(!ConnectToServer(gateway, 80))
    {
      Logger::logln("MON: Cannot connect, reinitialize ethernet.");
      Ethernet.begin(mac, ip, dns, gateway, subnet);
    }
  } else
  {
    // Send status all the time
    Logger::debug("MON: Sending status from ...");Logger::debugln(Ethernet.localIP());
    // Send the state of the pins
    MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(),arduinoClient);
    MyExecutor::sendToServer("END",arduinoClient);
  }

  // Finish Thread execution
  runned();  
  
  }