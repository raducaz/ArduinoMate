#include <ArduinoJson.h>
#include <SPI.h>
#include <Ethernet.h>
#include "Thread.h"
#include "ThreadController.h"
#include <TimerOne.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(192,168,11,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
byte serverIp[] = { 192, 168, 11, 99 };
//IPAddress serverIp = IPAddress(192,168,11,99);

ThreadController threadsController = ThreadController();
//volatile boolean monitorThreadRunning = 0;

void startThreadsController()
{
  // Is best practice to start the threadController from a Timer interrupt so we avoid blocking the main thread
  Timer1.stop();

  Timer1.initialize(500); // in micro second (us)
  Timer1.attachInterrupt(starterTimerCallback);
  Timer1.start();
}

class JsonBuilder
{
    StaticJsonBuffer<100> _buffer;
    JsonObject& _root;

public:
    JsonBuilder()
        : _root(_buffer.createObject())
    {
    }

    void addPinStates()
    {
      JsonArray& pinStates = _root.createNestedArray("pinStates");

      for(byte i=7;i<=8;i++)
      {
        addPin(pinStates, i);
      }
      
    }
    void addPin(JsonArray& pinStates, byte i)
    {
        JsonObject& pinState = pinStates.createNestedObject();
        pinState[getPinName(i)] = digitalRead(i);
    }

    void dumpTo(Print &destination) const
    {
        _root.printTo(destination);
    }

private:
    const char* getPinName(byte i)
    {
        char* key = (char*)_buffer.alloc(3);
        sprintf(key, "P%d", i);
        return key;
    }
};

class MyMonitorTcpClientThread: public Thread
{
  EthernetClient arduinoClient;
  //IPAddress serverIp = IPAddress(192,168,11,99); //<<< ENTER DEFAULT REMOTE SERVER IP ADDRESS HERE!!!
  
  String ReceiveMsgFromServer()
  {
    char endChar = '\n';
    String receivedText = "";
  
    if(arduinoClient)
    {
      while (arduinoClient.available()) {
         char receivedChar = arduinoClient.read();
         Serial.print(receivedChar);
    
         if (receivedChar==endChar){
          //TODO: Check the line received
          Serial.println(receivedText);
    
          // Reset the line received
          receivedText = "";
          Serial.println("END");
          break;
        }
        else
        {
          receivedText.concat(receivedChar);
        } 
       }
    }
  
     return receivedText;
  }
  public:boolean ConnectToServer()
  {
    if(arduinoClient)
    {
      Serial.println("MON: Stop client.");
      arduinoClient.stop();
      delay(1000);
    }
  
    Serial.println("MON: Connect client.");
    if (arduinoClient.connect(serverIp, 9090)) {
      Serial.println("MON: Client connected.");
      return arduinoClient.connected();
    }
    Serial.println("MON: Client dinn't connect.");
    return false;
  }
  public:boolean SendMsgToServer()
  {

//JsonBuilder* jBuilder = new JsonBuilder();
//jBuilder->addPinStates();
StaticJsonBuffer<100> _buffer;
JsonObject& _root = _buffer.createObject();
JsonArray& pinStates = _root.createNestedArray("pinStates");
for(byte i=7;i<=8;i++)
{
  JsonObject& pinState = pinStates.createNestedObject();
  char* key = (char*)_buffer.alloc(3);
  sprintf(key, "P%d", i);
  pinState[key] = digitalRead(i);
}
    
    Serial.print("MON: Send ");
    _root.printTo(Serial);Serial.println("");
 //   jBuilder->dumpTo(Serial);Serial.println("");
    
    if(arduinoClient.connected())
     {
        Serial.println("MON: Client is connected, send ");
//        jBuilder->dumpTo(arduinoClient);
//         arduinoClient.println(msg);
         // Success
         return true;
     }
     else
     {
        Serial.println("MON: Client not connected, try connect");
        // Retry once
        if(ConnectToServer())
        {
          Serial.println("MON: Client connected on retry");
//          jBuilder->dumpTo(arduinoClient);
//          arduinoClient.println(msg);
          // Success
          return true;
        }
        else
        {
          Serial.println("MON: Retry connect failed");
          // Failed
          return false;
        }
     }

//     delete jBuilder;

  }
    
  // Function executed on thread execution
  void run(){

    Serial.println("MON: exec monitor");

    // If needed the message from server
    //Serial.println(ReceiveMsgFromServer());
    
    if(SendMsgToServer())
    {
      Serial.println("MON:Sent msg to server.");
    }
    else
    {
      Serial.println("MON:FAILED Sent msg to server.");
    }

    // Finish Thread execution
    runned();  
  }
  
};

class MyTcpServerThread: public Thread
{
  EthernetServer server = EthernetServer(8080);
 
  // Function executed on thread execution
  void run(){

    server.begin();
  
    EthernetClient client = server.available();
    char endChar = '\n';
    char receivedText[] = ""; //safe to change despite char* receivedText="";
    
    if (client) {
      while (client.connected()) {
        if (client.available()) {

          Serial.println("Server started...listening...");
          
          char receivedChar = client.read();
          
          Serial.print(receivedChar);
  
          if (receivedChar==endChar)
          {
            Serial.print("CMD:");Serial.println(receivedText);
            
            if(strcmp(receivedText,"StateFct")==0)
            {
              client.print("Executing ");client.println(receivedText);
              Serial.print("Executing ");Serial.println(receivedText);

              if(digitalRead(7)==HIGH)
                digitalWrite(7, LOW);
              else
                digitalWrite(7, HIGH);
                
              client.print(receivedText);client.println("step 1");
              Serial.print(receivedText);Serial.println("step 1");
              
              client.print(receivedText);client.println("step 2");
              Serial.print(receivedText);Serial.println("step 2");
              
              client.println("END");
              Serial.println("END");
            }
  
            if(strcmp(receivedText,"MonitorFct")==0)
            {
//              //noInterrupts();
//              MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
//              if(!monitorThreadRunning)
//              {
//                client.println("Try connect to server");
//                if(monitorTcpClientThread.ConnectToServer())
//                {
//                  client.println("Connection to server success.");
//                }
//                else
//                {
//                  client.println("Connection to server failed.");
//                }
//  
//                  client.println("Starting monitor thread.");
//                  
//                  // Set the interval the thread should run in loop
//                  monitorTcpClientThread.setInterval(1000); // in ms
//
//                  // Add thread to controller, this will fire the thread automatically
//                  threadsController.add(&monitorTcpClientThread); 
//                  // Mark monitor started
//                  monitorThreadRunning = 1;
//                  
//                  //startThreadsController();
//    
//                  client.println("Monitor started");
//                  Serial.println("Monitor started.");
//                  
//                //interrupts();
//              }
//              else
//              {
//                client.println("Monitor is running");
//              }
              
              client.println("END");
            }
            
            strcpy(receivedText, "");
          }
          else
          {
            size_t len = strlen(receivedText);
            char* newReceived = new char[len+2];
            strcpy(newReceived,receivedText);
            newReceived[len]=receivedChar;
            newReceived[len]='\0';
            //delete receivedText;
            *receivedText = *newReceived;
            
          } 
        } 
      }
  
      Serial.println();
      Serial.println("CLOSE CONNECTION"); 
      client.stop();
      Serial.println("END LOOP");
  
    }
    else
    {
      // No client, server not available()
    }  

    runned();

  }
};

void starterTimerCallback(){
  threadsController.run();
  //Timer1.stop();
}

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(500); // in ms
  threadsController.add(&tcpServerThread);

  MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
  monitorTcpClientThread.setInterval(1000); // in ms
  threadsController.add(&monitorTcpClientThread);
}

void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);

  delay(1000);
  setupTcpServerThread();
  startThreadsController();

  

}

void loop() {
  
}

