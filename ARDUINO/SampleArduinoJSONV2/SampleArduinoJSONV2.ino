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

byte OnOffGeneratorState = 0; // 0=OFF,1=ON,2=Error
byte deviceState = 0;

ThreadController threadsController = ThreadController();

class MyTcpServerThread: public Thread
{
  EthernetServer server = EthernetServer(8080);

    // Function executed on thread execution
    void run(){
      server.begin();
      EthernetClient client = server.available();

      test(client);

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
      }; // wait until 
      
      Serial.print("done wait");Serial.println(millis());
  }  
  private: JsonObject& constructPinStatesJSON(const char* msg)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root["msg"] = msg;
    JsonArray& pinStates = _root.createNestedArray("pinStates");
    for(int i=0;i<=10;i++)
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
    JsonArray& pinState = _root.createNestedArray("pinState");
    pinState.add(pin);
    pinState.add(digitalRead(pin));
    
    return _root;
  }
  private: JsonObject& constructPinStateJSON(byte pin)
  {
    StaticJsonBuffer<200> _buffer;
    JsonObject& _root = _buffer.createObject();
    JsonArray& pinState = _root.createNestedArray("pinState");
    pinState.add(pin);
    pinState.add(digitalRead(pin));
    
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
  private: void setPin(byte pin, byte state, EthernetClient& client)
  {
    digitalWrite(pin, state);
    if(client.connected())
    {
      constructPinStateJSON(pin).printTo(Serial);
    }
  }
  private: void setPinTemp(byte pin, byte state, unsigned int interval, EthernetClient& client)
  {
    if(client.connected())
    {
      constructPinStateJSON(pin).printTo(Serial);
    }
    
    byte state1 = digitalRead(pin);
    digitalWrite(pin, state);
    wait(interval);
    digitalWrite(pin, state1);
    
    if(client.connected())
    {
      constructPinStateJSON(pin).printTo(Serial);
    }
  }
  void test(EthernetClient& client)
  {
      constructPinStatesJSON("cici").printTo(Serial);
      Serial.println();
      setPinTemp(ActuatorNormal, LOW, 500, client);
      wait(1000);
      constructPinStateJSON(2,"cici").printTo(Serial);
      Serial.println();
      constructPinStateJSON(2).printTo(Serial);
      setPin(ContactDemaror12V, LOW, client);
      Serial.println();
      constructFctStateJSON(1, "fct").printTo(Serial);
      Serial.println();
  }
  
};

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(1000); // in ms
  threadsController.add(&tcpServerThread);

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
  
}

void loop() {
  
    delay(500);
    //Start the Thread in loop
    threadsController.run();
    delay(500);
}

