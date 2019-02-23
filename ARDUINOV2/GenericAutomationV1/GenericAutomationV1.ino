#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>

#include <SPI.h>
#include "ACS712.h"

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

/*=Globals=============================================================================================*/
// Enter a MAC address and IP address for your controller below.
/* TEST */
byte mac[] = { 0x78, 0x24, 0xaf, 0x3a, 0xa6, 0x77 };
IPAddress            ip(192,168,100,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
const char myIp[16]  = "192.168.100.100";
/* TEST */
int myPort = 8080;

/* PROD */
//byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x03 };
//IPAddress            ip(192,168,1,200); //<<< ENTER YOUR IP ADDRESS HERE!!!
//const char myIp[15]  = "192.168.1.200";
/* PROD */

byte serverIp[] = { 192, 168, 100, 3 };
int serverPort = 9090;
byte gateway[] = { 192, 168, 100, 1 };
byte dns[] = { 192, 168, 100, 1 };
byte subnet[] = { 255, 255, 255, 0 };

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A5);
float zeroCurrent = 0;

ThreadController threadsController = ThreadController();

volatile byte DeviceState = 0; // 0=READY,1=BUSY,2=ERROR
const char MSG[4] = "msg";
const char PIN[4] = "pin";
const char IP[3] = "ip";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char PINSTATES[10] = "pinStates";
/*=====================================================================================================*/

/*=Classes=============================================================================================*/
class Configuration
{
  public:static void setupPins()
  {
    // OUTPUT PINS
    pinMode(2, INPUT_PULLUP); //Sets it to HIGH
    pinMode(3, OUTPUT);
  }
  public:static void initializePins()
  {  
    digitalWrite(2, HIGH); 
  }
};

class JSONSerializer
{
  public: static JsonObject& constructPinStatesJSON()
  {
    return constructPinStatesJSON("");
  }
  public: static JsonObject& constructPinStatesJSON(const char* msg)
  {
    StaticJsonBuffer<400> _buffer;
    JsonObject& _root = _buffer.createObject();
    if(strcmp(msg,"")!=0)
      _root[MSG] = msg;
    _root[IP] = myIp;
    _root[DEVICESTATE] = DeviceState;
    JsonArray& pinStates = _root.createNestedArray(PINSTATES);
    for(byte i=0;i<10;i++)
    {
      pinStates.add(digitalRead(i));
    }
    
    return _root;
  }
  public: static JsonObject& constructPinStateJSON(byte pin, const char* msg)
  {
    StaticJsonBuffer<400> _buffer;
    JsonObject& _root = _buffer.createObject();
    if(strcmp(msg,"")==0)
      _root[MSG] = msg;
    _root[PIN] = pin;
    _root[IP] = myIp;
    _root[DEVICESTATE] = DeviceState;
    _root[VALUE] = digitalRead(pin);
    
    return _root;
  }
  public: static JsonObject& constructPinStateJSON(byte pin)
  {
    return constructPinStateJSON(pin, "");
  }
  public: static JsonObject& constructFctStateJSON(byte state, const char* msg, const char* fct)
  {
    StaticJsonBuffer<400> _buffer;
    JsonObject& _root = _buffer.createObject();
    _root[MSG] = msg;
    _root[IP] = myIp;
    _root[DEVICESTATE] = DeviceState;
    _root[FCTNAME] = fct;
    _root[FCTSTATE] = state;
    
    return _root;
  }
};

class MyExecutor
{
  public: static void wait(unsigned int msInterval)
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

  public: static void sendToServer(const char* msg, EthernetClient& client)
  {
    client.println(msg);

    Serial.println(msg);
  }
  public: static void sendToServer(JsonObject& json, EthernetClient& client)
  {
    json.printTo(client);
    client.println();

    json.printTo(Serial);
    Serial.println();
  }
  private: static void setPin(byte pin, byte state, EthernetClient& client)
  {
    //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.println(state);
    digitalWrite(pin, state);
  }
  private: static void setPinTemp(byte pin, byte state, unsigned int interval, EthernetClient& client)
  {
    //Serial.print("Set pin:");Serial.print(pin);Serial.print(" to ");Serial.print(state);Serial.print(" for ");Serial.print(interval);Serial.println(" ms");
    if(client.connected())
    {
      //sendToServer(JSONSerializer::constructPinStateJSON(pin), client);
    }
    
    byte state1 = digitalRead(pin);
    digitalWrite(pin, state);
    wait(interval);
    digitalWrite(pin, state1);
  }
  public: static void sampleFunction(EthernetClient& client)
  {
    sendToServer(JSONSerializer::constructPinStatesJSON("Contact->ON,Starter->ONOFF,Soc->OFF"),client);
    sendToServer(JSONSerializer::constructFctStateJSON(1, "Generator=ON", FCTNAME),client);
    setPinTemp(1, LOW, 500, client);
  }
 
  private: static bool testConnectionBetween(byte senderPin, byte receiverPin)
  {
    //byte mode1 = pinMode(receiverPin);
    pinMode(receiverPin, INPUT_PULLUP); //Sets it to HIGH
    
    digitalWrite(senderPin,LOW); // Set sender to ground
    wait(100);
    if(digitalRead(receiverPin)==LOW)
    {
      wait(100);
      if(digitalRead(receiverPin)==LOW) //Double check
      {
        Serial.println("Pressure low.");
        digitalWrite(senderPin,HIGH);// Revert the state of Sender
        
        return true;
      }
    } 

    return false;
  }
};
class MyMonitorTcpClientThread: public Thread
{
  EthernetClient arduinoClient;
  
  public:boolean ConnectToServer(byte* ip, int port)
  {
    if(arduinoClient)
    {
      arduinoClient.stop();
    }
  
    if (arduinoClient.connect(ip, port)) {
      return arduinoClient.connected();
    }

    // Failed to connect
    return false;
  }
    
  // Function executed on thread execution
  void run(){

    int i = 0;
    while(!(arduinoClient.connected()) && (i < 2))
    {
      ConnectToServer(serverIp, serverPort);
      i++;
    }
    if(!arduinoClient.connected())
    {
//      Serial.println("MON: Check connection to gateway.");
      if(!ConnectToServer(gateway, 80))
      {
        Serial.println("MON: Cannot connect, reinitialize ethernet.");
        Ethernet.begin(mac, ip, dns, gateway, subnet);
      }
    } else
    {
      // Send status all the time
      Serial.print("MON: Sending status from ...");Serial.println(Ethernet.localIP());
      // Send the state of the pins
      MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(),arduinoClient);
      MyExecutor::sendToServer("END",arduinoClient);
    }

    // Finish Thread execution
    runned();  
  }
};

class MyTcpServerThread: public Thread
{
    // Function executed on thread execution
    void run(){

      //Serial.println("Start Server to listen clients...");
      EthernetServer server = EthernetServer(myPort);
      server.begin();
    
      EthernetClient client = server.available();
      char endChar = '\n';
      const byte SIZE = 50;
      char receivedText[SIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
      
//      Serial.println("Server started...listening...");
      if (client) 
      {
        while (client.connected()) 
        {
          if (client.available()) 
          {
            char receivedChar = client.read();
            
//            Serial.println(receivedChar);
    
            if (receivedChar==endChar)
            {
              processCommand(receivedText, client);
              
              // End communication with client - for any function
              MyExecutor::sendToServer("END", client);
              
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
  void processCommand(const char* commandText, EthernetClient& client)
  {
    Serial.print("CMD:");Serial.println(commandText);
              
    if(strcmp(commandText,"GeneratorOnOff")==0)
    {
      
    }
    else if(strcmp(commandText,"PowerOnOff")==0)
    {
      
    }
  }
  
};
/*=====================================================================================================*/

/*=Methods=============================================================================================*/
void calibrateCurrentSensor()
{
  // calibrate() method calibrates zero point of sensor,
  // It is not necessary, but may positively affect the accuracy
  // Ensure that no current flows through the sensor at this moment
  // If you are not sure that the current through the sensor will not leak during calibration - comment out this method
  sensor.calibrate();

  float c = 0;
  for(int i=0;i<10;i++)
  {
    c += sensor.getCurrentAC();
    delay(100);
  }
  zeroCurrent = c / 10;
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

/*=Entry=Point=============================================================================================*/
void setup() {
  Serial.begin(9600);
  Serial.println("Entering Setup");

  Configuration::setupPins();
  Configuration::initializePins();

  delay(1000);
  calibrateCurrentSensor();
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);
  delay(1000);
  Serial.print("My IP address: ");
  Serial.println(Ethernet.localIP());
  
  setupTcpServerThread();
}

void loop() {
  
    delay(500);
    //Start the Thread in loop
    threadsController.run();
    delay(500);
}
/*==============================================================================================*/
