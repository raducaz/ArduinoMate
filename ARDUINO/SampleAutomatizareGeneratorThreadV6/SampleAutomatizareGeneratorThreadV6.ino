#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

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
byte serverIp[] = { 192, 168, 1, 99 };

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 5; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

bool cycleDone = true;
bool generatorPornit = false;

ThreadController threadsController = ThreadController();

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
    Serial.println("MON: Client didn't connect.");
    return false;
  }
  private: JsonObject& constructJSON()
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["ip"] = "192.168.1.100";
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(byte i=0;i<=13;i++)
    {
      //JsonObject& pinState = pinStates.createNestedObject();
      //char* key = (char*)_buffer.alloc(3);
      //sprintf(key, "p%d", i);
      //pinState[key] = digitalRead(i);
      pinStates.add(digitalRead(i));
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
            
            if(strcmp(receivedText,"OnOffGenerator")==0)
            {
              if(generatorPornit == false)
              {
                pornire(client);
              }
              else
              {
                oprire(client);
              }
            }
            else if(strcmp(receivedText,"MonitorFct")==0)
            {
              // DO nothing ... 
            }
            else if(strcmp(receivedText,"TestWaitFct")==0)
            {
              testWait(client);
            }

            // End communication with client - for any function
            client.println("END");
            
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

void wait(unsigned int msInterval)
{
    unsigned long waitStart = millis();
    Serial.print("wait");Serial.println(msInterval);Serial.println(waitStart);

    unsigned long current = millis();
    while((current - waitStart)< msInterval) 
    {
      current = millis();
      //Serial.print("Current:");Serial.println(current);
    }; // wait until 
    
    Serial.print("done wait");Serial.println(millis());
}  
private: JsonObject& constructJSON(const char* msg)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["ip"] = "192.168.1.100";
    _root["msg"] = msg;
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(byte i=2;i<=7;i++)
    {
      //if(i==4) continue;
      
      //JsonObject& pinState = pinStates.createNestedObject();
      //char* key = (char*)_buffer.alloc(3);
      //sprintf(key, "p%d", i);
      //pinState[key] = digitalRead(i);
      //delete[] key;
      pinStates.add(digitalRead(i));
    }
    
    return _root;
  }
void testWait(EthernetClient& client)
{
  client.println("Start");
  wait(40000);
  client.println("Stop");
}
void pornire(EthernetClient& client)
{
  if(!generatorPornit)
  {
    // Tras soc
    digitalWrite(ActuatorNormal, LOW); // Cuplare
    digitalWrite(ActuatorInversat, HIGH); // DECUPLARE
    //client.println("Actuator - PORNIRE INAINTE");
    
    constructJSON("Actuator - PORNIRE INAINTE").printTo(client);
    wait(500);
    digitalWrite(ActuatorNormal, HIGH);// DECUPLARE
    digitalWrite(ActuatorInversat, HIGH);// DECUPLARE
   
    constructJSON("Actuator - OPRIT").printTo(client);
    //client.println("Actuator - OPRIT");
    wait(2000);

    // Punere contact
    digitalWrite(ContactGenerator, LOW); //CUPLARE releu = intrerupere circuit contact pentru pornire generator
    
    constructJSON("Contact - ON").printTo(client);
    //client.println("Contact - ON");
    wait(2000);

    // Contact motor
    digitalWrite(ContactDemaror12V, HIGH); // CUPLARE
    //client.println("ContactDemaror12V - PORNIRE");
    wait(4000);
    digitalWrite(ContactDemaror12V, LOW); // DECUPLARE
    //client.println("ContactDemaror12V - OPRIRE");
    wait(2000);

    // Scoatere soc
    digitalWrite(ActuatorNormal, HIGH); // DECUPLARE
    digitalWrite(ActuatorInversat, LOW); // CUPLARE 
    //client.println("Actuator - PORNIRE INAPOI");
    wait(500);
    digitalWrite(ActuatorNormal, HIGH); // DECUPLARE
    digitalWrite(ActuatorInversat, HIGH); // DECUPLARE
    //client.println("Actuator - OPRIT");

    //TODO: Testare prezenta curent 220 - trebuie consumator pe priza
    // In cazul in care nu este curent se initiaza procedura de inchidere generator

    // Cuplare priza 220V iesire
    wait(2000);
    digitalWrite(ContactRetea220V, LOW); // CUPLARE
    //client.println("ContactRetea220V - PORNIRE");    
  }

  generatorPornit = true;
  client.println("GENERATOR - PORNIT !!!");
}

void oprire(EthernetClient& client)
{
    digitalWrite(ContactRetea220V, HIGH); //DECUPLARE
    client.println("ContactRetea220V - OPRIT");
    wait(2000);
    
    // Punere contact
    digitalWrite(ContactGenerator, HIGH); //DECUPLARE releu = inchidere circuit contact pentru oprire generator
    client.println("Contact - OFF");
    wait(2000);
    client.println("ContactGenerator - OPRIT");

    generatorPornit = false;
    client.println("GENERATOR - OPRIT !!!");

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
  tcpServerThread.setInterval(1000); // in ms
  threadsController.add(&tcpServerThread);

  MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread();
  monitorTcpClientThread.setInterval(500); // in ms
  threadsController.add(&monitorTcpClientThread);
}

void setup() {
  Serial.begin(9600);

  // OUTPUT PINS
  pinMode(ContactGenerator, OUTPUT);
  pinMode(ActuatorNormal, OUTPUT);
  pinMode(ActuatorInversat, OUTPUT);
  pinMode(ContactRetea220V, OUTPUT);
  pinMode(ContactDemaror12V, OUTPUT);
  
  digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
  digitalWrite(ActuatorNormal, HIGH); // Decuplat 
  digitalWrite(ActuatorInversat, HIGH); // Decuplat
  digitalWrite(ContactRetea220V, HIGH); // Decuplat
  digitalWrite(ContactDemaror12V, LOW); // Decuplat
  
  // start the Ethernet connecti  on and the server:
  Ethernet.begin(mac, ip);
  delay(1000);
  
  setupTcpServerThread();
  // Do not start with Timer - it breaks the millis function
  //startThreadsController();
}
boolean threadsStarted=0;
void loop() {
  if(!threadsStarted)
  {
    
    threadsStarted = 1;
  }
  delay(500);
    //Start the Thread in loop
    threadsController.run();
    delay(500);
}

