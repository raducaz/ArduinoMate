#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

#include <TimerOne.h>

#include <Dns.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include <EthernetUdp.h>

#include <SPI.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

// Enter a MAC address and IP address for your controller below.
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(192,168,1,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
byte serverIp[] = { 192, 168, 1, 168 };

ThreadController threadsController = ThreadController();

void startThreadsController()
{
  // Is best practice to start the threadController from a Timer interrupt so we avoid blocking the main thread
  Timer1.stop();

  Timer1.initialize(500); // in micro second (us)
  Timer1.attachInterrupt(starterTimerCallback);
  Timer1.start();
}

class MyMonitorTcpClientThread: public Thread
{
  EthernetClient arduinoClient;
  
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
  private: JsonObject& constructJSON()
  {
    StaticJsonBuffer<100> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["ip"] = "192.168.1.100";
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(byte i=7;i<=8;i++)
    {
      JsonObject& pinState = pinStates.createNestedObject();
      char* key = (char*)_buffer.alloc(3);
      sprintf(key, "p%d", i);
      pinState[key] = digitalRead(i);
    }

    return _root;
  }
  public:boolean SendMsgToServer()
  {
    JsonObject& _root = constructJSON();
    
    Serial.print("MON: Send ");
    _root.printTo(Serial);Serial.println("");
     
    if(arduinoClient.connected())
     {
        Serial.println("MON: Client is connected, send ");
        _root.printTo(arduinoClient);
        arduinoClient.println("END");
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
          _root.printTo(arduinoClient);
          arduinoClient.println("END");
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
    const byte SIZE = 50;
    char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
    
    Serial.println("Server started...listening...");
    if (client) {
      while (client.connected()) {
        if (client.available()) {
          
          char receivedChar = client.read();
          
          Serial.println(receivedChar);
  
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
              // DO nothing ... 
                
              client.println("END");
            }
            
            strcpy(receivedText, "\0");
          }
          else
          {
            size_t len = strlen(receivedText);
            if (len < SIZE)
            {
              receivedText[len] = receivedChar;
              receivedText[len + 1] = '\0';
            }
            else
            {
              Serial.println("Max received message len riched.");
            }
          } 
        } 
      }
  
      Serial.println();
      Serial.println("CLOSE CONNECTION"); 
      client.stop();
      Serial.println("END LOOP");

      //delete[] receivedText;
  
    }
    else
    {
      // No client, server not available()
      Serial.println("No client, server stopped.");
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
  
  setupTcpServerThread();
  startThreadsController();
}

void loop() {
  
}

