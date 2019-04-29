
// #include "tcpserver.h"

// #include <Arduino.h>
// #include <Thread.h>
// #include <Ethernet.h>
// #include <EthernetServer.h>
// #include <executor.h>


// MyTcpServerThread::MyTcpServerThread() {;}
// MyTcpServerThread::~MyTcpServerThread() {;}
// //EthernetServer server = EthernetServer(myPort);

// // Function executed on thread execution
// void MyTcpServerThread::run(){

//   //Serial.println("Start Server to listen clients...");
//   EthernetServer server = EthernetServer(8080);
//   server.begin();

//   EthernetClient client = server.available();
//   char endChar = '\n';
//   const byte SIZE = 50;
//   char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
  
//       // Serial.println("Server started...listening...");
//   if (client) 
//   {
//     while (client.connected()) 
//     {
//       if (client.available()) 
//       {
//         char receivedChar = client.read();
        
//             Serial.println(receivedChar);

//         if (receivedChar==endChar)
//         {
//           processCommand(receivedText, client);
          
//           // End communication with client - for any function
//           MyExecutor::sendToServer("END", client);
          
//           strcpy(receivedText, "\0");
//         }
//         else
//         {
//           size_t len = strlen(receivedText);
//           if (len < SIZE-1)
//           {
//             receivedText[len] = receivedChar;
//             receivedText[len + 1] = '\0';
//           }
//           else
//           {
//             Serial.println("Max received message len reached.");
//           }
//         } 
//       } 
//     }

//     Serial.println();
//     Serial.println("CLOSE CONNECTION"); 
//     client.stop();
//     //Serial.println("END LOOP");

//   }
//   else
//   {
//     // No client, server not available()
// //        Serial.println("No client, server stopped.");
//   }  

//   runned();
// }
// void MyTcpServerThread::processCommand(const char* commandText, EthernetClient& client)
// {
// Serial.print("CMD:");Serial.println(commandText);
          
// if(strcmp(commandText,"GeneratorOnOff")==0)
// {
//     //MyExecutor::setPin(3, 1, client);
  
// }
// }
