#include "tcpclient.h"

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <jsonhelper.cpp>
#include <executor.h>

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
  Serial.println("Try connect to ");
  for(int i=0;i<4;i++){ Serial.print(ip[i]); Serial.print("."); }
     Serial.println("");

  if(arduinoClient)
  {
    arduinoClient.stop();
  }

  if (arduinoClient.connect(ip, port)) {    
    return arduinoClient.connected();
  }

  // Failed to connect
//    Serial.println("MON: Client didn't connect to ");
//    for(int i=0;i<4;i++){ Serial.print(ip[i]);}
//    Serial.println("");
  
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
//      Serial.println("MON: Check connection to gateway.");
    if(!ConnectToServer(gateway, 80))
    {
      Serial.println("MON: Cannot connect, reinitialize ethernet.");
      Ethernet.begin(mac, ip, dns, gateway, subnet);
    }
  } else
  {
    // Send status all the time
    Serial.print("MON: Sending status from ...");Serial.println(Ethernet.localIP());
    // Send the state of the pins
    MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(),arduinoClient);
    MyExecutor::sendToServer("END",arduinoClient);
  }

  // Finish Thread execution
  runned();  
  
  }