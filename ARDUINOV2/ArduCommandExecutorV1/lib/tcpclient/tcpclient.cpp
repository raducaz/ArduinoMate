#include "tcpclient.h"
#include "globals.h"

#include <Arduino.h>
#include <Thread.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <jsonhelper.cpp>
#include <executor.h>

boolean MyMonitorTcpClientThread::ConnectToServer(EthernetClient arduinoClient, const byte* ip, const int port)
{
  if(arduinoClient)
  {
    arduinoClient.stop();
  }

  if (arduinoClient.connect(ip, port)) {
    
//      for(int i=0;i<4;i++){ Serial.print(ip[i]);}
//      Serial.println("");
    
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
  
  EthernetClient arduinoClient;

  int i = 0;
  while(!(arduinoClient.connected()) && (i < 2))
  {
    ConnectToServer(arduinoClient, serverIp, serverPort);
    i++;
  }
  if(!arduinoClient.connected())
  {
//      Serial.println("MON: Check connection to gateway.");
    EthernetClient arduinoClient2;
    if(!ConnectToServer(arduinoClient2, gateway, 80))
    {
      Serial.println("MON: Cannot connect, reinitialize ethernet.");
      Ethernet.begin(mac, ip, dns, gateway, subnet);
    }
  } else
  {
  
  }

  // Send status all the time
  Serial.print("MON: Sending status from ...");Serial.println(Ethernet.localIP());
  // Send the state of the pins
  MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(),arduinoClient);
  MyExecutor::sendToServer("END",arduinoClient);
    

  // Finish Thread execution
  runned();  
  
  }