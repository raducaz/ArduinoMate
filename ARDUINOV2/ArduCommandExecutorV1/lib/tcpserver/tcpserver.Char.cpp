
// #include "tcpserver.h"

// #include <ArduinoJson.h>
// #include <Arduino.h>
// #include <Thread.h>
// #include <Ethernet.h>
// #include <EthernetServer.h>
// #include <executor.h>
// #include "logger.h"
// #include "configuration.h"


// MyTcpServerThread::MyTcpServerThread() {;}
// MyTcpServerThread::~MyTcpServerThread() {;}

// // Function executed on thread execution
// void MyTcpServerThread::run(){

//   if(Configuration::useEthernet()){
//     this->listenEthernet();
//   }
//   else
//   {
//     this->listenSerial();
//   }
  
//   runned();
// }
// void MyTcpServerThread::listenSerial()
// {
//   char endChar = '\n';
//   const byte SerialSize = 64; // This is Serial.read limit
  
//   if(Serial){

//     if (Serial.available()!=0) {
//       char c = 0;
//       bool endCmd = false;
//       byte i=0;
//       while(((c=Serial.read())!=endChar) && i<SerialSize ){
//         buffer[bufferSize]=c;
//         buffer[bufferSize+1]='\0';
//         bufferSize++;
        
//         if(c==']' || bufferSize+1>=MAXBUFFERSIZE)
//         {
//           endCmd=true;
//           break;
//         }

//         i++;
//       }
        
//       //Logger::debugln(receivedText.as<char*>());
//       Serial.println("");
//       Serial.println(buffer);

//       //TODO: Test - this executes actual commands and blocks the thread until done
//       if(endCmd)
//       {
//         char* result = this->parseCommand(buffer);
//         Serial.println(result);
//         buffer[0]=0;
//         bufferSize=0;
//       }
//     }
//   }
// }
// void MyTcpServerThread::listenEthernet()
// {
//   //Serial.println("Start Server to listen clients...");
//   EthernetServer server = EthernetServer(8080);
//   server.begin();

//   EthernetClient client = server.available();
//   Logger::debugln("Server started...listening...");

//   char endChar = '\n';
//   const byte SIZE = 50;
//   char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
  
// //      Serial.println("Server started...listening...");
//   if (client) 
//   {
//     while (client.connected()) 
//     {
//       if (client.available()) 
//       {
//         char receivedChar = client.read();
//         if (receivedChar==endChar)
//         {
//           parseCommand(receivedText);
          
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
//             Serial.println("Max received message len riched.");
//           }
//         } 
//       } 
//     }

//     Serial.println();
//     Serial.println("CLOSE CONNECTION"); 
//     client.stop();
//   }
//   else
//   {
//     Logger::debugln("No client, server stopped");
//   }
// }
// char* MyTcpServerThread::parseCommand(char* plainJson)
// {
//   // [
//   //   {"=3":0.25,"@":2}, // for 2 ms and revert
//   //   {"=13":0},//set indefinite
//   //   {"!":20} // wait for 20 ms
//   //   {"get/13":"?"}
//   // ]
//   // [{"=3":1,"@":2},{"?13":0},{"=13":0},{"?13":0},{"!":20}]
//   // [set 3=1 for 2, get 13, set 13 to 0, get 13, wait 20]
//     const int capacity = JSON_ARRAY_SIZE(10) + 10*JSON_OBJECT_SIZE(2) + 32;
//     StaticJsonBuffer<capacity> jb;

//     // Parse JSON object
//     JsonArray& arr = jb.parseArray(plainJson);

//     if (arr.success()) {
//       for (JsonVariant& elem : arr) {
//         if (elem.is<JsonObject>()) {
//           JsonObject& obj = elem.as<JsonObject>();

//           int pin=0;
//           for (JsonPair& p : obj) {
//             //p.key is a const char* pointing to the key
//             if(strncmp(p.key,"=",1)==0)
//             {
//               pin = getPin(1, p.key);
//               if(obj["@"]){
//                 MyExecutor::setPinTemp(pin,p.value,obj["@"].as<int>());
//                 obj[">"] = 1;
//               }
//               else
//               {
//                 MyExecutor::setPin(pin, p.value);
//                 obj[">"] = 1;
//               }
//             }
//             if(strncmp(p.key,"?",1)==0)
//             {
//               pin = getPin(1, p.key);
//               obj[p.key] = digitalRead(pin);
//             }
//             if(strcmp(p.key,"!")==0){
//               MyExecutor::wait(p.value);
//               obj[">"] = 1;
//             }
//           }
         
//         }
//       }

//       char output[50]; 
//       arr.printTo(output);
//       return output;

//     } else {
//       // parseObject() failed
//       Logger::debugln("Deserialize received message failed.");
//       return "";
//     }
// }
// int MyTcpServerThread::getPin(const byte size, const char* key)
// {
//   char sPin[2]="";
//   byte i=0;
//   while(key[size+i]){
//     sPin[i]=key[size+i];
//     i++;
//   }
//   sPin[i] = '\0';
//   return atoi(sPin);
// }
