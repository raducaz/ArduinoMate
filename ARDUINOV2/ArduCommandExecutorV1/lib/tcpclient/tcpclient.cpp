// #include "tcpclient.h"

// #include <Arduino.h>
// #include <Thread.h>
// #include <Ethernet.h>
// #include <EthernetClient.h>
// #include <jsonhelper.cpp>
// #include <executor.h>
// #include "logger.h"
// #include <ACS712.h>

// MyMonitorTcpClientThread::MyMonitorTcpClientThread(
//                                         const byte* ip, 
//                                         byte* mac, 
//                                         byte* serverIp, 
//                                         int serverPort, 
//                                         byte* gateway, byte* dns, byte* subnet,
//                                         float zeroCurrent
//                                         ):Thread()
// {
//   this->ip = ip;
//   this->mac = mac;
//   this->serverIp = serverIp;
//   this->serverPort = serverPort;
//   this-> gateway = gateway;
//   this->dns = dns;
//   this->subnet = subnet;

//   this->zeroCurrent = zeroCurrent;
// }
// boolean MyMonitorTcpClientThread::ConnectToServer(const byte* ip, const int port)
// {
//   if(arduinoClient)
//   { 
//     if(!arduinoClient.connected())
//     {
//       arduinoClient.stop();
      
//       Logger::debugln("MON: Reconnecting...");
//       if (arduinoClient.connect(ip, port)) {    
//         return arduinoClient.connected();
//       }
//       else
//       {
//         return false;
//       }
//     }
//     else
//     {
//       return true;
//     }
    
//   }
//   else
//   {
//     Logger::debugln("MON: Connecting...");
//     arduinoClient.connect(ip, port);
//     return arduinoClient.connected();
//   }
// }
  
// // Function executed on thread execution
// void MyMonitorTcpClientThread::run(){
  
//   int i = 0;
//   while(!(arduinoClient.connected()) && (i < 2))
//   {
//     ConnectToServer(serverIp, serverPort);
//     i++;
//   }
//   if(!arduinoClient.connected())
//   {
//     Logger::debugln("MON: Check connection to gateway.");
//     if(!ConnectToServer(gateway, 80))
//     {
//       Logger::debugln("MON: Cannot connect, reinitialize ethernet.");
//       Ethernet.begin(mac, ip, dns, gateway, subnet);
//     }
//   } else
//   {
//     // Send status all the time
//     Logger::debug("MON: Sending status from ...");Logger::debugln(Ethernet.localIP());
//     // Send the state of the pins
//     float digitalPinStates[14];
//     for(byte i=0;i<14;i++)
//     {
//         digitalPinStates[i] = digitalRead(i);
//     }
//     MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(ip, 0, 0, digitalPinStates, 14),arduinoClient);

//     float analogPinStates[6];
//     for(byte i=0;i<=5;i++)
//     {
//         analogPinStates[i] = analogRead(i+14);
//     }
//     ACS712 sensor(ACS712_30A, A1);
//     analogPinStates[1] = sensor.getCurrentAC()-zeroCurrent;

//     MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(ip, 0, 1, analogPinStates, 6),arduinoClient);
//     MyExecutor::sendToServer("END",arduinoClient);
//   }

//   // Finish Thread execution
//   runned();  
  
//   }