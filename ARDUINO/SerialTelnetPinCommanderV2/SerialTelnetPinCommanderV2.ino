#include <SPI.h>

#include <Thread.h>
#include <ThreadController.h>

#include <Ethernet.h>
#include <EthernetServer.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

bool debug=1;
/*Pins 0,1 are used by Serial cmyIpommunication via USB*/
/*Pins 10,11,12,13 are user by Etherne Shield */

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 8; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 6; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 7; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

byte OnOffGeneratorState = 0; // 0=OFF,1=ON,2=Error

// Enter a MAC address and IP address for your controller below.
/* TEST */
//byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
//IPAddress ip(192,168,1,100); //<<< ENTER YOUR IP ADDRESS HERE!!!
/* TEST */
/*PROD*/
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x03 };
IPAddress ip(192,168,1,200); //<<< ENTER YOUR IP ADDRESS HERE!!!
/*PROD*/

ThreadController threadsController = ThreadController();
class MyTcpServerThread: public Thread
{
  static const byte SIZE = 10;
  char receivedText[SIZE] = "";
  char bufferText[SIZE] = "";
  char cmdChar = 0; 
  int pin = 0;
  
    char endChar = '\n';
    EthernetClient client = EthernetClient();
    
    // Function executed on thread execution
    void run(){
      
    if(debug) Serial.println("TCP: Start server.");
    EthernetServer server = EthernetServer(8080);
    server.begin(); 
    client = server.available();

    if (client) {
      if(debug) Serial.println("TCP:Client exists.");
      client.println("2=1 to set pin 2 to 1; ls to list all pins; use 2/1 to set pin 2 to 1 for 500ms"); //Prompt User for input
      client.println("Serial commands cannot be sent until end of this session."); //Prompt User for input
      while (client.connected()) {
        if(debug) Serial.println("TCP: Client connected.");
        if (client.available()) {
          if(debug) Serial.println("TCP: Client available.");
          char receivedChar = client.read();

          if(collectInput(receivedChar)) {
            // Text until \n received
            client.println("Exec telnet CMD, see Serial monitor for details");
            Serial.println("Exec telnet CMD.");

            execCmd();

            if(strncmp(receivedText, "end", 3)==0){
              Serial.println("CLOSE CONNECTION");
              client.println("CLOSE CONNECTION");
              client.stop();
              Serial.println("END LOOP");
            }
            
          }
          
        } 
      } 
      
//      Serial.println();
//      Serial.println("CLOSE CONNECTION"); 
      //client.stop();
      if(debug) Serial.println("TCP:END LOOP");

    }
    else
    {
      // No client, server not available()
      if(debug) Serial.println("TCP:No client, server stopped.");
    }  

    if (Serial.available()!=0) {             //Wait for user input
      char receivedChar = Serial.read();
      Serial.print(receivedChar);
      
      if(collectInput(receivedChar)){
        Serial.println("Exec Serial CMD.");
        execCmd();
      }
      
    }

    runned();
  }
  bool collectInput(const char receivedChar)
  {
    if (receivedChar=='\n')
    {
      receivedText[0]=0;
      strcpy(receivedText, bufferText);
      bufferText[0]=0;

      Serial.print("Got:");Serial.print(receivedText);Serial.print(" CMD:");Serial.print(cmdChar);Serial.print(" Pin:");Serial.println(pin);
      client.print("Got:");client.print(receivedText);client.print(" CMD:");client.print(cmdChar);client.print(" Pin:");client.println(pin);
      
      return true;
    }
    else
    {
      if(receivedChar=='=' || receivedChar=='?' || receivedChar=='/')
      {
        cmdChar = receivedChar;

        if(bufferText[0]==0)
          pin = -1;
        else
          pin = atoi(bufferText);
          
        bufferText[0]= 0;// Empty string
      }
      else
      {
        size_t len = strlen(bufferText);
        if (len < SIZE-1)
        {
          bufferText[len] = receivedChar;
          bufferText[len + 1] = 0;
        }
        else
        {
          Serial.println("Max meesage len exceeded !! Clearing receivedText.");
          bufferText[0]=0;
        }
      }
    }

     return false;
  }
  void setPin(int pin, int value)
  {
      Serial.print("set pin:");Serial.print(pin);
      Serial.print(" to value:");Serial.println(value);
      
      client.print("set pin:");client.print(pin);
      client.print(" to value:");client.println(value);
    
      digitalWrite(pin,value);
  }
  void setPinTemp(int pin, int value, int ms)
  {
      byte v = digitalRead(pin);
      
      Serial.print("set pin:");Serial.print(pin);
      Serial.print(" to value (for ");Serial.print(ms);Serial.print("ms):");Serial.println(value);
      
      client.print("set pin:");client.print(pin);
      client.print(" to value (for ");client.print(ms);client.print("ms):");client.println(value);
      
      digitalWrite(pin,value);
      wait(ms);
      digitalWrite(pin,v);
  
      Serial.print("reverted pin:");Serial.print(pin);
      Serial.print(" to value:");Serial.println(v);
      
      client.print("reverted pin:");client.print(pin);
      client.print(" to value:");client.println(v);
  }
  void getPin(int pin)
  {
      Serial.print("pin:");Serial.print(pin);
      Serial.print(" is:");Serial.println(digitalRead(pin));
      
      client.print("pin:");client.print(pin);
      client.print(" is:");client.println(digitalRead(pin));
  }
  void execCmd()
  {
    int value = 0;
    
    if(strncmp(receivedText, "ls", 2)==0)
    {
      for(byte i=0;i<=13;i++)
      {
        Serial.print(i);Serial.print("=");Serial.println(digitalRead(i));
        client.print(i);client.print("=");client.println(digitalRead(i));
        
      }
    }
    else if(strncmp(receivedText, "reset", 5)==0){
     initializePins(); 
     
     for(byte i=0;i<=13;i++)
      {
        Serial.print(i);Serial.print("=");Serial.println(digitalRead(i));
        client.print(i);client.print("=");client.println(digitalRead(i));
        
      }
      
      Ethernet.begin(mac, ip);
    }
    else if(strncmp(receivedText, "on", 2)==0){
      generatorON();
    }
    else if(strncmp(receivedText, "off", 3)==0){
      generatorOFF();
    }
    else if(cmdChar == '=')
    {
      value = atoi(receivedText);

      for(int i=(pin==-1?0:pin);i<=(pin==-1?13:pin);i++)
      {
        setPin(i, value);
      }
    }
    else if(cmdChar == '/')
    {
      value = atoi(receivedText);
    
      for(int i=(pin==-1?0:pin);i<=(pin==-1?13:pin);i++)
      {
        setPinTemp(i, value, 500);
      }
    }
    else if(cmdChar == '?')
    {
      for(int i=(pin==-1?0:pin);i<=(pin==-1?13:pin);i++)
      {
        getPin(i);
      }
    }
    else
    {
      Serial.println("no command");
    }

    // Reset command
    cmdChar=0;
  }
  void wait(unsigned int msInterval)
  {
      unsigned long waitStart = millis();
      //Serial.print("wait:");Serial.print(msInterval);Serial.println(waitStart);
  
      unsigned long current = millis();
      while((current - waitStart)< msInterval) 
      {
        current = millis();
        //Serial.print("Current:");Serial.println(current);
      }; // wait until 
      
     // Serial.print("done wait");Serial.println(millis());
  } 
void generatorON()
  {
    if(OnOffGeneratorState==0) //Only if it's OFF
    {
      // Tras soc
      client.println("Soc->ON");
      setPinTemp(ActuatorNormal, LOW, 500);
      client.println("Soc=ON");
      wait(1000);
  
        // Punere contact
        client.println("Contact->ON,Starter->ONOFF,Soc->OFF");
        setPin(ContactGenerator, LOW);//CUPLARE releu = intrerupere circuit contact pentru pornire generator
        client.println("Contact=ON");
        
        wait(1000);
    
        // Contact motor - for 2 seconds
        setPinTemp(ContactDemaror12V, HIGH, 1000);
        
      // Scoatere soc
      setPinTemp(ActuatorInversat, LOW, 500);
      client.println("Starter=OFF");
      client.println("Soc=OFF");
  
      //TODO: Testare prezenta curent 220 - trebuie consumator pe priza
      // In cazul in care nu este curent se initiaza procedura de inchidere generator  

      OnOffGeneratorState = 1;
      client.println("Generator=ON");
    }
  
  }
  
  void generatorOFF()
  {
      client.println("220->OFF");
      setPin(ContactRetea220V, HIGH);//DECUPLARE
      client.println("220=OFF");  
      wait(2000);
      
      // Oprire contact
      client.println("Contact->OFF");
      setPin(ContactGenerator, HIGH);//DECUPLARE releu = inchidere circuit contact pentru oprire generator
      client.println("Contact=OFF");
      
      OnOffGeneratorState = 0;
      client.println("Generator=OFF");
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

void setup() {
  Serial.begin(9600);

  // OUTPUT PINS
//  pinMode(1, OUTPUT);
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
//  pinMode(10, OUTPUT);
//  pinMode(11, OUTPUT);
//  pinMode(12, OUTPUT);
//  pinMode(13, OUTPUT);

MyTcpServerThread::initializePins();

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  delay(1000);
  setupTcpServerThread();

  Serial.println("2=1 to set pin 2 to 1; ls to list all pins; use 2/1 to set pin 2 to 1 for 500ms"); //Prompt User for input
  Serial.println("telnet 192.168.1.100 8080 - monitor output in Serial Monitor."); //Prompt User for input
}
void setupTcpServerThread()
{
  if(debug) Serial.println("TCP: Setup server.");
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(1); // in ms
  threadsController.add(&tcpServerThread);
  if(debug) Serial.println("TCP: End Setup server.");
}

void loop() {
  if(debug) Serial.println("Enter Loop");
  threadsController.run();
  if(debug) Serial.println("Exit Loop");
}

