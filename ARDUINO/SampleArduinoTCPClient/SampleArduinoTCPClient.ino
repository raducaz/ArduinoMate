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

volatile ThreadController threadsController = ThreadController();
volatile boolean monitorThreadRunning = 0;

void startThreadsController()
{
  Serial.println("Starting thread starter ...");
  // Is best practice to start the threadController from a Timer interrupt so we avoid blocking the main thread
  Timer1.stop();

  Timer1.initialize(2000); // in micro second (us)
  Timer1.attachInterrupt(starterTimerCallback);
 
  Timer1.start();
}

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
        Serial.print("Reply form server");
        Serial.println(receivedText);
  
        // Reset the line received
        receivedText = "";
        Serial.println("END reply");
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
    Serial.println("Client already connected: Stop client.");
    arduinoClient.stop();
    delay(1000);
  }

  Serial.println("Connecting client...");
  if (arduinoClient.connect(serverIp, 9090)) {
    Serial.println("Client connected ");
//    Serial.println("Ip-" + serverIp);
    
    return arduinoClient.connected();
  }
  
  Serial.println("Error connecting client ");
//  Serial.println("ip-" + serverIp);
  return false;
}
public:boolean SendMsgToServer(String msg)
{
  Serial.println("Start sending to server: " + msg);
  
  if(arduinoClient.connected())
   {
       Serial.println("Client is connected, send: " + msg);
       arduinoClient.println(msg);
       Serial.println("Message sent.");
       // Success
       return true;
   }
   else
   {
      Serial.println("Client not connected, try connect");
      
      // Retry once
      if(ConnectToServer())
      {
        Serial.println("RETRY: Client connected on retry");
        Serial.println("RETRY: Sending message:" + msg);
        
        arduinoClient.println(msg);
        Serial.println("RETRY: Message sent");
        // Success
        return true;
      }
      else
      {
        Serial.println("RETRY: error connecting, connect failed");
        // Failed
        return false;
      }
   }
}
    
  // Function executed on thread execution
  void run(){

    Serial.println("Thread started....");

    // If needed the message from server
    //Serial.println(ReceiveMsgFromServer());
    
    String msg = "test";
    Serial.println("Message prepared: " + msg);
    
    if(SendMsgToServer(msg))
    {
      Serial.println("FINAL: Message sent to server.");
    }
    else
    {
      Serial.println("FINAL: FAILED sending msg to server.");
    }

    // Finish Thread execution -
    //runned();  //let this run recurrent as setInterval states
  }
  
};

volatile MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();

void starterTimerCallback(){
  
  setupTcpServerThread();
  
  Serial.println("Starting client...");
  threadsController.run();
  
  //Timer1.stop();
}

void setupTcpServerThread()
{
  Serial.println("Setup client...");
  threadsController = ThreadController();
  MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
  monitorTcpClientThread.setInterval(2000); // in ms
  threadsController.add(&monitorTcpClientThread);
}

void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);

  delay(1000);
  startThreadsController();

}

void loop() {
  
}
