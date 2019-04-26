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

/*Pins 0,1 are used by Serial cmyIpommunication via USB*/
/*Pins 10,11,12,13 are user by Etherne Shield */

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

const int PresostatProbeSender = A3;
const int PresostatProbeReceiver = A4;
const int CurrentSensor = A5;

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A5);
float zeroCurrent = 0;

byte OnOffGeneratorState = 0; // 0=OFF,1=ON,2=Error,255=Executing
byte OnOffPowerState = 0; // 0=OFF,1=ON,2=Error
byte OnOffPowerAutoState = 0; // 0=Disabled,1=Enabled,2=Error
volatile byte DeviceState = 0; // 0=READY,1=BUSY,2=ERROR

ThreadController threadsController = ThreadController();

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
    pinMode(ContactGenerator, OUTPUT);
    pinMode(ActuatorNormal, OUTPUT);
    pinMode(ActuatorInversat, OUTPUT);
    pinMode(ContactRetea220V, OUTPUT);
    pinMode(ContactDemaror12V, OUTPUT);
    pinMode(PresostatProbeSender, OUTPUT);
    pinMode(PresostatProbeReceiver, INPUT_PULLUP); //Sets it to HIGH
    pinMode(CurrentSensor, OUTPUT);
  }
  public:static void initializePins()
  {  
    digitalWrite(ContactGenerator, HIGH); // Cuplat = contact OFF
    digitalWrite(ActuatorNormal, HIGH); // Decuplat 
    digitalWrite(ActuatorInversat, HIGH); // Decuplat
    digitalWrite(ContactRetea220V, HIGH); // Decuplat
    digitalWrite(ContactDemaror12V, LOW); // Decuplat
    digitalWrite(PresostatProbeSender, HIGH); // This will be our ground when probing, until then let it HIGH
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
  public: static void generatorON(EthernetClient& client)
  {
    const char FCTNAME[15] = "GeneratorOnOff";
    if(OnOffGeneratorState==0) //Only if it's OFF
    {
      // Tras soc
      sendToServer(JSONSerializer::constructPinStatesJSON("Soc->ON"),client);
      setPinTemp(ActuatorNormal, LOW, 500, client);
      sendToServer(JSONSerializer::constructPinStatesJSON("Soc=ON"),client);
      wait(1000);
  
        // Punere contact
        sendToServer(JSONSerializer::constructPinStatesJSON("Contact->ON,Starter->ONOFF,Soc->OFF"),client);
        setPin(ContactGenerator, LOW, client);//CUPLARE releu = intrerupere circuit contact pentru pornire generator
        sendToServer(JSONSerializer::constructPinStatesJSON("Contact=ON"),client);
        
        wait(1000);
    
        // Contact motor - for 2 seconds
        setPinTemp(ContactDemaror12V, HIGH, 2000, client);
        
      // Scoatere soc
      setPinTemp(ActuatorInversat, LOW, 500, client);
      sendToServer(JSONSerializer::constructPinStatesJSON("Starter=OFF"),client);
      sendToServer(JSONSerializer::constructPinStatesJSON("Soc=OFF"),client);
  
//      // Testare prezenta curent 220 - trebuie consumator pe priza
//      // In cazul in care nu este curent se initiaza procedura de inchidere generator  
//      float currentAC = sensor.getCurrentAC()-zeroCurrent;
//      if(currentAC<=0.1) // a relevant current value for a led bulb permanently connected to the circuit of the board
//      {
//        // Stop generator to ensure doesn't remain on because of a sensor error
//        generatorOFF(client);
//        
//      }
//      else
//      {
        OnOffGeneratorState = 1;
        sendToServer(JSONSerializer::constructFctStateJSON(OnOffGeneratorState, "Generator=ON", FCTNAME),client);
//      }
    }
  
  }
  
  public: static void generatorOFF(EthernetClient& client)
  {
    const char FCTNAME[15] = "GeneratorOnOff";
    
      sendToServer(JSONSerializer::constructPinStatesJSON("220->OFF"),client);
      setPin(ContactRetea220V, HIGH, client);//DECUPLARE
      sendToServer(JSONSerializer::constructPinStatesJSON("220=OFF"),client);  
      wait(2000);
      
      // Oprire contact
      sendToServer(JSONSerializer::constructPinStatesJSON("Contact->OFF"),client);
      setPin(ContactGenerator, HIGH, client);//DECUPLARE releu = inchidere circuit contact pentru oprire generator
      sendToServer(JSONSerializer::constructPinStatesJSON("Contact=OFF"),client);

      // Reset pins to initial state when stop generator
      Configuration::initializePins();

      // Testare prezenta curent 220 - trebuie consumator pe priza
      // In cazul in care este curent se seteaza error state  
      float currentAC = sensor.getCurrentAC()-zeroCurrent;
      if(currentAC>0.1) // a relevant current value for a led bulb permanently connected to the circuit of the board
      {
        OnOffGeneratorState = 2;
      }
      else
      {
        OnOffGeneratorState = 0;
      }
      
      sendToServer(JSONSerializer::constructFctStateJSON(OnOffGeneratorState, "Generator=OFF", FCTNAME),client);
  }

  public: static void powerON(EthernetClient& client)
  {
    const char FCTNAME[11] = "PowerOnOff";
    
    if(OnOffGeneratorState==1)
    {
      // Cuplare priza 220V iesire
      sendToServer(JSONSerializer::constructPinStatesJSON("220->ON"),client);   
      setPin(ContactRetea220V, LOW, client); // CUPLARE
      sendToServer(JSONSerializer::constructPinStatesJSON("220=ON"),client); 

      OnOffPowerState = 1;
      sendToServer(JSONSerializer::constructFctStateJSON(OnOffPowerState, "Power=ON", FCTNAME),client);
    } else
    {
      OnOffPowerState = 0;
      sendToServer(JSONSerializer::constructFctStateJSON(OnOffPowerState, "Power=OFF because generator OFF", FCTNAME),client);
    }
  }
  public: static void powerOFF(EthernetClient& client)
  {
    const char FCTNAME[11] = "PowerOnOff";
    
    sendToServer(JSONSerializer::constructPinStatesJSON("220->OFF"),client);
    setPin(ContactRetea220V, HIGH, client);//DECUPLARE
    sendToServer(JSONSerializer::constructPinStatesJSON("220=OFF"),client);  

    OnOffPowerState = 0;
    sendToServer(JSONSerializer::constructFctStateJSON(OnOffPowerState, "Power=OFF", FCTNAME),client);
  }
  public: static void powerAutoENABLE(EthernetClient& client)
  {
    const char FCTNAME[23] = "PowerAutoEnableDisable";

    //Serial.println("Enable power auto: power off");
    // Make sure to start from virgin grounds
    powerOFF(client);
    //Serial.println("Enable power auto: generator off");
    generatorOFF(client);
    
    OnOffPowerAutoState = 1;
    DeviceState = 1;
    
    sendToServer(JSONSerializer::constructFctStateJSON(OnOffPowerAutoState, "PowerAuto=Enabled", FCTNAME),client);
    //Serial.println("Enable power auto: execute first time");
    //powerAutoEXECUTE(client); //Wait for the executor to do the job
  }
  public: static void powerAutoDISABLE(EthernetClient& client)
  {
    const char FCTNAME[23] = "PowerAutoEnableDisable";
    
    // Make sure to let virgin grounds behind us
    powerOFF(client);
    generatorOFF(client);
    
    OnOffPowerAutoState = 0;
    DeviceState = 0;

    sendToServer(JSONSerializer::constructFctStateJSON(OnOffPowerAutoState, "PowerAuto=Disabled", FCTNAME),client);
  }
  private: static bool isPressureLow()
  {
    digitalWrite(PresostatProbeSender,LOW); //Probe if Presostat is activated - presure low. Set sender to ground
    wait(100);
    if(digitalRead(PresostatProbeReceiver)==LOW)
    {
      wait(100);
      if(digitalRead(PresostatProbeReceiver)==LOW) //Doube check
      {
        Serial.println("Pressure low.");
        digitalWrite(PresostatProbeSender,HIGH);// Revert the state of Sender
        
        return true;
      }
    } 

    return false;
  }
  public: static void powerAutoEXECUTE(EthernetClient& client)
  {
    if(OnOffPowerAutoState == 1)
    {
        if(OnOffGeneratorState==0) 
        {
          Serial.println("Gen off - power off");
          //Make sure priza is off as well
          powerOFF(client);
          
          if(isPressureLow())
          {  
            generatorON(client);
            powerON(client);
          } 
          //Serial.println("Probe inactive, exit.");
        } 
        else if(OnOffGeneratorState==1)
        {
          Serial.println("Generator on");
          if(OnOffPowerState==0)
          {
            //Serial.println("Power off, starting power");
            // Make sure the priza is on as well
            powerON(client);
          }

          //TEST
          if(!isPressureLow())
          {  
            generatorOFF(client);
            powerOFF(client);
          } 
          //TEST
          
          if(OnOffPowerState==1)
          {
//            Serial.println("Power on, probing current.");
//            float current = sensor.getCurrentAC();
//            if((current - zeroCurrent) < 0.1) //there is no current flowing
//            {
//              Serial.println("No current, stop power.");
//              powerOFF(client);
//              generatorOFF(client);
//            }

            //Serial.println("There is current, let power on.");
          }
          
        }
    }
    Serial.println("Exit execution.");
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
      
//      for(int i=0;i<4;i++){ Serial.print(ip[i]);}
//      Serial.println("");
      
      return arduinoClient.connected();
    }

    // Failed to connect
//    Serial.println("MON: Client didn't connect to ");
//    for(int i=0;i<4;i++){ Serial.print(ip[i]);}
//    Serial.println("");
    
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
      // Execute auto functions
      if(DeviceState == 1) // Device is in executing state
      {
        // TODO: Check what happens on the server if END is not sent ...
        // The state of the pins are sent by the functions them selves
        Serial.println("Execute AUTO function.");
        executeAuto();
      }

      // Send status all the time
      Serial.print("MON: Sending status from ...");Serial.println(Ethernet.localIP());
      // Send the state of the pins
      MyExecutor::sendToServer(JSONSerializer::constructPinStatesJSON(),arduinoClient);
      MyExecutor::sendToServer("END",arduinoClient);
      
    }

    // Finish Thread execution
    runned();  
  }

  void executeAuto()
  {
    MyExecutor::powerAutoEXECUTE(arduinoClient);
    //TODO: add all functions that have auto
  }
};

class MyTcpServerThread: public Thread
{
    //EthernetServer server = EthernetServer(myPort);
    
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
      if(OnOffGeneratorState == 0)
      {
        MyExecutor::generatorON(client);
      }
      else if(OnOffGeneratorState == 1)
      {
        MyExecutor::generatorOFF(client);
      }
      else // if error state
      { 
        //TODO: reset device state
      }
    }
    else if(strcmp(commandText,"PowerOnOff")==0)
    {
      if(OnOffPowerState==0)
      {
        MyExecutor::powerON(client);
      }
      else
      {
        MyExecutor::powerOFF(client);
      }
    }
    else if(strcmp(commandText,"PowerAutoEnableDisable")==0)
    {
      if(OnOffPowerAutoState==0)
      {
        MyExecutor::powerAutoENABLE(client);
      }
      else
      {
        MyExecutor::powerAutoDISABLE(client);
      }
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
