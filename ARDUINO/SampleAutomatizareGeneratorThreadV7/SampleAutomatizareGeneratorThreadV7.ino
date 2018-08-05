#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>

#include <SPI.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

// Enter a MAC address and IP address for your controller below.
/* TEST */
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress            ip(192,168,1,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
const char myIp[15]  = "192.168.1.100";
/* TEST */
int myPort = 8080;

/* PROD */
//byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x03 };
//IPAddress            ip(192,168,1,200); //<<< ENTER YOUR IP ADDRESS HERE!!!
//const char myIp[15]  = "192.168.1.200";
/* PROD */

byte serverIp[] = { 192, 168, 1, 168 };
int serverPort = 9090;
byte gateway[] = { 192, 168, 1, 1 };
byte subnet[] = { 255, 255, 255, 0 };


/*Pins 0,1 are used by Serial cmyIpommunication via USB*/
/*Pins 10,11,12,13 are user by Etherne Shield */

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

byte OnOffGeneratorState = 0; // 0=OFF,1=ON,2=Error
byte OnOffPrizaState = 0; // 0=OFF,1=ON,2=Error
byte deviceState = 0;

ThreadController threadsController = ThreadController();

class MyMonitorTcpClientThread: public Thread
{
  EthernetClient arduinoClient;
  
  public:boolean ConnectToServer(byte* ip, int port)
  {
    if(arduinoClient)
    {
//      Serial.println("MON: Stop client.");
      arduinoClient.stop();
//      delay(1000);
    }
  
//    Serial.println("MON: Connect client.");
    if (arduinoClient.connect(ip, port)) {
      Serial.print("MON: Client connected to ");
      for(int i=0;i<4;i++){ Serial.print(ip[i]);}
      Serial.println("");
      
      return arduinoClient.connected();
    }
    
    Serial.println("MON: Client didn't connect to ");
    for(int i=0;i<4;i++){ Serial.print(ip[i]);}
    Serial.println("");
    
    return false;
  }
  private: JsonObject& constructJSON()
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["ip"] = myIp;
    _root["state"] = deviceState;
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(byte i=0;i<10;i++)
    {
      pinStates.add(digitalRead(i));
    }

    return _root;
  }
  public:boolean SendMsgToServer()
  {
    JsonObject& _root = constructJSON();
    
//    Serial.print("MON: Send ");
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
        if(ConnectToServer(serverIp, serverPort))
        {
          Serial.println("MON: Client connected on retry");
          _root.printTo(arduinoClient);
          arduinoClient.println("END");
          // Success
          return true;
        }
        else
        {
          Serial.println("MON: Retry connect failed.");
          if(!ConnectToServer(gateway, 80))
          {
            Serial.println("MON: Reinitialize.");
            Ethernet.begin(mac, ip, gateway, subnet);
          }
          else
            arduinoClient.stop();  
          // Failed
          return false;
        }
     }
    
  }
    
  // Function executed on thread execution
  void run(){

//    Serial.println("MON: exec monitor");

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
    EthernetServer server = EthernetServer(myPort);
    // Function executed on thread execution
    void run(){

      Serial.println("Restart Server...");
      server = EthernetServer(myPort);
      server.begin();
    
      EthernetClient client = server.available();
      char endChar = '\n';
      const byte SIZE = 50;
      char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
      
//      Serial.println("Server started...listening...");
      if (client) {
        while (client.connected()) {
          if (client.available()) {
            
            char receivedChar = client.read();
            
//            Serial.println(receivedChar);
    
            if (receivedChar==endChar)
            {
              Serial.print("CMD:");Serial.println(receivedText);
              
              if(strcmp(receivedText,"OnOffGenerator")==0)
              {
                if(OnOffGeneratorState == 0)
                {
                  generatorON(client);
                }
                else if(OnOffGeneratorState == 1)
                {
                  generatorOFF(client);
                }
                else // if error state
                { //TODO: reset device state
                }
              }
              else if(strcmp(receivedText,"OnOffPriza")==0)
              {
                if(OnOffPrizaState==0)
                {
                  onPriza(client);
                }
                else
                {
                  offPriza(client);
                }
              }
              
              // End communication with client - for any function
              client.println("END");
              
              strcpy(receivedText, "\0");
            }
            else
            {
              size_t len = strlen(receivedText);
              if (len < SIZE-1)
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
        //Serial.println("END LOOP");
  
      }
      else
      {
        // No client, server not available()
//        Serial.println("No client, server stopped.");
      }  

      runned();
  }

  void wait(unsigned int msInterval)
  {
      unsigned long waitStart = millis();
//      Serial.print("wait");Serial.println(msInterval);Serial.println(waitStart);
  
      unsigned long current = millis();
      while((current - waitStart)< msInterval) 
      {
        current = millis();
        //Serial.print("Current:");Serial.println(current);
      }; // wait until 
      
//      Serial.print("done wait");Serial.println(millis());
  }  
  private: JsonObject& constructPinStatesJSON(const char* msg)
  {
    StaticJsonBuffer<400> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["msg"] = msg;
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(byte i=0;i<10;i++)
    {
      pinStates.add(digitalRead(i));
    }
    
    return _root;
  }
  private: JsonObject& constructPinStateJSON(byte pin, const char* msg)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["msg"] = msg;
    _root["pin"] = pin;
    _root["value"] = digitalRead(pin);
    
    return _root;
  }
  private: JsonObject& constructPinStateJSON(byte pin)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["pin"] = pin;
    _root["value"] = digitalRead(pin);
    
    return _root;
  }
  private: JsonObject& constructFctStateJSON(byte state, const char* msg)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["msg"] = msg;
    _root["fctState"] = state;
    
    return _root;
  }
  void sendToServer(JsonObject& json, EthernetClient& client)
  {
    json.printTo(client);
    client.println();

    json.printTo(Serial);
    Serial.println();
  }
  private: void setPin(byte pin, byte state, EthernetClient& client)
  {
    //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.println(state);
    digitalWrite(pin, state);
  }
  private: void setPinTemp(byte pin, byte state, unsigned int interval, EthernetClient& client)
  {
    //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.print(state);Serial.print(" for ");Serial.print(interval);Serial.println(" ms");
    if(client.connected())
    {
      sendToServer(constructPinStateJSON(pin), client);
    }
    
    byte state1 = digitalRead(pin);
    digitalWrite(pin, state);
    wait(interval);
    digitalWrite(pin, state1);
  }
  void generatorON(EthernetClient& client)
  {
    if(OnOffGeneratorState==0) //Only if it's OFF
    {
      // Tras soc
      sendToServer(constructPinStatesJSON("Soc->ON"),client);
      setPinTemp(ActuatorNormal, LOW, 500, client);
      sendToServer(constructPinStatesJSON("Soc=ON"),client);
      wait(1000);
  
        // Punere contact
        sendToServer(constructPinStatesJSON("Contact->ON,Starter->ONOFF,Soc->OFF"),client);
        setPin(ContactGenerator, LOW, client);//CUPLARE releu = intrerupere circuit contact pentru pornire generator
        sendToServer(constructPinStatesJSON("Contact=ON"),client);
        
        wait(1000);
    
        // Contact motor - for 2 seconds
        setPinTemp(ContactDemaror12V, HIGH, 2000, client);
        
      // Scoatere soc
      setPinTemp(ActuatorInversat, LOW, 500, client);
      sendToServer(constructPinStatesJSON("Starter=OFF"),client);
      sendToServer(constructPinStatesJSON("Soc=OFF"),client);
  
      //TODO: Testare prezenta curent 220 - trebuie consumator pe priza
      // In cazul in care nu este curent se initiaza procedura de inchidere generator  

      OnOffGeneratorState = 1;
      sendToServer(constructFctStateJSON(OnOffGeneratorState, "Generator=ON"),client);
    }
  
  }
  
  void generatorOFF(EthernetClient& client)
  {
      sendToServer(constructPinStatesJSON("220->OFF"),client);
      setPin(ContactRetea220V, HIGH, client);//DECUPLARE
      sendToServer(constructPinStatesJSON("220=OFF"),client);  
      wait(2000);
      
      // Oprire contact
      sendToServer(constructPinStatesJSON("Contact->OFF"),client);
      setPin(ContactGenerator, HIGH, client);//DECUPLARE releu = inchidere circuit contact pentru oprire generator
      sendToServer(constructPinStatesJSON("Contact=OFF"),client);

      // Reset pins to initial state when stop generator
      initializePins();
      
      OnOffGeneratorState = 0;
      sendToServer(constructFctStateJSON(OnOffGeneratorState, "Generator=OFF"),client);
  }

  void onPriza(EthernetClient& client)
  {
    if(OnOffGeneratorState==1)
    {
      // Cuplare priza 220V iesire
      sendToServer(constructPinStatesJSON("220->ON"),client);   
      setPin(ContactRetea220V, LOW, client); // CUPLARE
      sendToServer(constructPinStatesJSON("220=ON"),client); 

      OnOffPrizaState = 1;
      sendToServer(constructFctStateJSON(OnOffPrizaState, "Priza220=ON"),client);
    } else
    {
      OnOffPrizaState = 0;
      sendToServer(constructFctStateJSON(OnOffPrizaState, "Priza220=OFF because generator OFF"),client);
    }
  }
  void offPriza(EthernetClient& client)
  {
    sendToServer(constructPinStatesJSON("220->OFF"),client);
    setPin(ContactRetea220V, HIGH, client);//DECUPLARE
    sendToServer(constructPinStatesJSON("220=OFF"),client);  

    OnOffPrizaState = 0;
    sendToServer(constructFctStateJSON(OnOffPrizaState, "Priza220=OFF"),client);
  }
  public:static void initializePins()
  {  
    digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
    digitalWrite(ActuatorNormal, HIGH); // Decuplat 
    digitalWrite(ActuatorInversat, HIGH); // Decuplat
    digitalWrite(ContactRetea220V, HIGH); // Decuplat
    digitalWrite(ContactDemaror12V, LOW); // Decuplat
  }
};

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
Serial.println("Entering Setup");

  // OUTPUT PINS
  pinMode(ContactGenerator, OUTPUT);
  pinMode(ActuatorNormal, OUTPUT);
  pinMode(ActuatorInversat, OUTPUT);
  pinMode(ContactRetea220V, OUTPUT);
  pinMode(ContactDemaror12V, OUTPUT);
  
  MyTcpServerThread::initializePins();
  
  // start the Ethernet connecti  on and the server:
  Ethernet.begin(mac, ip, gateway, subnet);
  delay(1000);
  
  setupTcpServerThread();
  
}

void loop() {
  
    delay(500);
    //Start the Thread in loop
    threadsController.run();
    delay(500);
}

