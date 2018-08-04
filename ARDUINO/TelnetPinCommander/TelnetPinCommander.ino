#include <Thread.h>
#include <ThreadController.h>

#include <Dns.h>
#include <Ethernet.h>
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

ThreadController threadsController = ThreadController();
class MyTcpServerThread: public Thread
{
    EthernetServer server = EthernetServer(8080);
   
    // Function executed on thread execution
    void run(){

    server.begin();
  
    EthernetClient client = server.available();
    char endChar = '\n';
    char cmdChar = '\0';
    const byte SIZE = 10;
    char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
    int pin = 0;
    int value = 0;
    
    Serial.println("Server started...listening...");
    if (client) {
      client.print("you are connected. type 2=1 to set pin 2 to 1 or 2? to get pin 2 value.");
      
      while (client.connected()) {
        if (client.available()) {
          
          char receivedChar = client.read();
          
          Serial.println(receivedChar);
  
          if (receivedChar==endChar)
          {
            if(strncmp(receivedText,"exit",4)==0)
            {
              strcpy(receivedText, "\0");
              break;
            }
            else if(strncmp(receivedText, "ls", 2)==0)
            {
              for(int i=0;i<=13;i++)
              {
                client.print(i);client.print("=");client.println(digitalRead(i));
              }
            }
            else if(cmdChar == '=')
            {
              value = atoi(receivedText);
            
              client.print("set pin:");client.print(pin);
              client.print(" to value:");client.println(value);
            
              digitalWrite(pin,value);
            }
            else if(cmdChar == '?')
            {
              client.print("pin:");client.print(pin);
              client.print(" is:");client.println(digitalRead(pin));
            }
            else
            {
              client.println("no command");
            }
            
            client.println("END");
            Serial.println("END");
          
            strcpy(receivedText, "\0");
          }
          else
          {
            if(receivedChar=='=' || receivedChar=='?')
            {
              cmdChar = receivedChar;
              
              pin = atoi(receivedText);
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
      }
  
      Serial.println();
      Serial.println("CLOSE CONNECTION"); 
      client.println("CLOSE CONNECTION");
      client.stop();
      Serial.println("END LOOP");
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
}

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(500); // in ms
  threadsController.add(&tcpServerThread);
}

void setup() {
  //Serial.begin(9600);

  // OUTPUT PINS
  pinMode(1, OUTPUT);
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);
  
  setupTcpServerThread();
  startThreadsController();
}

void loop() {
  
}

