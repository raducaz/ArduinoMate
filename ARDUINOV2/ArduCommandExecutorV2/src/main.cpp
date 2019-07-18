#include <Arduino.h>
#include <MemoryFree.h>

#include <math.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include "globals.h"
#include <SPI.h>
#include "log.h"

#ifdef HASTEMP
  #include "Adafruit_Sensor.h"
  #include "DHT.h"
#endif
#ifdef HASCURRENT
  #include <ACS712.h>
#endif
#include <executor.h>
#include <stateconstructor.h>
#include <getCommandParam.h>
#include <appendCommand.h>

#ifdef HASTEMP
  #define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321
  DHT dht(TemperatureSensor, DHTTYPE);
#endif
#ifdef HASCURRENT
  // We have 30 amps version sensor connected to A5 pin of arduino
  ACS712 sensor(ACS712_30A, A1);
  float zeroCurrent = 0;
#endif


byte noLoopRuns = 0;
bool isRestarted = false;
EthernetClient arduinoClient;
EthernetServer server = EthernetServer(arduinoPort);

// Reset function
void(* resetFunc) (void) = 0;

void calibrateCurrentSensor()
{
  #ifdef HASCURRENT
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
    zeroCurrent = c / 10.0;
  #endif
}
void f0()
{
  resetFunc();
}
float f1()
{
  #if defined HASCURRENT
    return sensor.getCurrentAC()-zeroCurrent;
  #else
    return 0;
  #endif
}
int f2()
{
  #if defined HASTEMP
    // To ensure temp can be read using pins 0 and 1 need to Serial.end();
    float t = dht.readTemperature();

    // Check if any reads failed and exit early (to try again).
    if (isnan(t)) {
      return -100;
    }
    return (int)t;
  #else
    return 0;
  #endif
}

void sendToServer(const char* msg)
{
  arduinoClient.println(msg);

  Log::debugln(msg);
}

void parseCommand(char* plainJson)
{
  // [=3:0.25:2] - set analog 3 to 0.25 for 2 ms (setdigital if digital pin)
  // [~3:0.25:2] - set digital 3 to 0.25 for 2 ms 
  // [=3:0.25] - set analog 2 to ...
  // [~3:0.25] - set digital
  // [!20] - wait for 20 ms
  // [?3] - digital read (including analog pins)
  // [#3] - analog read if analog
  // [F0] - run f0()
  // [=A3:1023:2|!20|?A3]

  Log::debugln(F("FreeMem:"), freeMemory());

  size_t len = strlen(plainJson);
  char cmd[15] = ""; //ex: =3:13:2000
  char res[MAXBUFFERSIZE] = "["; //return get pin values

  if(plainJson[0]=='['&&plainJson[len-1]==']')
  {
    byte i = 0;
    while(plainJson[i]>0)
    {
      if(plainJson[i]=='|' || plainJson[i]==']') // cmd terminator
      {
          if(strlen(cmd)==0) //empty command
            break;

          // SET
          if(cmd[0]=='=' || 
                cmd[0]=='~') //Cmd set
          {
            int pin = getCmdParam(cmd,0, true);
            int value = getCmdParam(cmd,1, false);
            int interval = getCmdParam(cmd,2, false);
            Log::debugln(F("pin:"), pin);
            Log::debugln(F("value:"), value);
            Log::debugln(F("interval:"), interval);

            if(pin<0 || value<0){
              strcpy(res, "PARSE_ERROR");
              break;
            }

            if(interval>0){ 
              // Cmd set temp for x ms
              if(cmd[0]=='~') 
              { 
                setDigitalPinTemp(pin,value,interval);
              }
              else
              {
                if(pin<=13) setDigitalPinTemp(pin,value,interval);
                else setAnalogPinTemp(pin,value,interval);  
              }
            }
            else // Cmd permanent set
            {
              if(cmd[0]=='~') 
              {
                setDigitalPin(pin, value);
              }
              else
              {
                if(pin<=13) setDigitalPin(pin, value);
                else setAnalogPin(pin, value);
              }
            }
          }
          
          // GET
          if(cmd[0]=='?'){
            int pin = getCmdParam(cmd,0, true);
            Log::debugln(F("pin:"), pin);
            if(pin<0){
              strcpy(res, "PARSE_ERROR");
              break;
            }

            if(!appendCmdResult(res, cmd, digitalRead(pin)))
              break;
          }
          // GET
          if(cmd[0]=='#'){
            int pin = getCmdParam(cmd,0, true);
            Log::debugln(F("pin:"), pin);
            if(pin<0){
              strcpy(res, "PARSE_ERROR");
              break;
            }
            if(!appendCmdResult(res, cmd, (pin<=13 ? digitalRead(pin) : analogRead(pin))))
              break;
          }
          // WAIT
          if(cmd[0]=='!'){ // Cmd wait
            int interval = getCmdParam(cmd,0, false);
            Log::debugln(F("interval:"), interval);
            if(interval<0){
              strcpy(res, "PARSE_ERROR");
              break;
            }
            wait(interval);
          }
          // FUNCTIONS
          if(cmd[0]=='F'){ 
            // Cmd function
            int fctNo = getCmdParam(cmd,0, false);
            Log::debugln(F("fct:"), fctNo);
            if(fctNo<0){
              strcpy(res, "PARSE_ERROR");
              break;
            }
            if(fctNo==0)
              f0();
            if(fctNo==1)
              if(!appendCmdResult(res,cmd,f1()))
                break;
            if(fctNo==2)
              if(!appendCmdResult(res,cmd,f2()))
                break;
          }
          
          // Clear received temp to be prepared to receive next command
          strcpy(cmd, "\0");
      }
      else{
        if(plainJson[i]!='['){
          size_t len = strlen(cmd);
          cmd[len] = plainJson[i];
          cmd[len + 1] = '\0';
        }
      }

      i++;
    }

    strcat(res,"]"); //End output message for completeness controll
    
  } else {
    Log::debugln(F("Deserialize received message failed."));
    strcat(res, "PARSE_ERROR]");
  }

  strcpy(plainJson, res);

  Log::debugln(F("FreeMem:"), freeMemory());

}
void listenSerial()
{
  char buffer[MAXBUFFERSIZE] = ""; 
  unsigned int bufferSize = 0;

  Log::debugln(F("Listen Serial"));

  char endChar = '\n';
  const byte SerialSize = 64; // This is Serial.read limit
  
  if(Serial){

    if (Serial.available()!=0) {
      Log::debugln(F("Serial available"));

      char c = 0;
      bool endCmd = false;
      byte i=0;
      while(((c=Serial.read())!=endChar) && i<SerialSize ){

        Serial.print(c);

        buffer[bufferSize]=c;
        buffer[bufferSize+1]='\0';
        bufferSize++;
        
        if(c==']' || bufferSize+1>=SerialSize || bufferSize+1>=MAXBUFFERSIZE)
        {
          endCmd=true;
          break;
        }

        i++;
      }
        
      //Log::debugln(receivedText.as<char*>());
      if(endCmd)
      {
        parseCommand(buffer);//this fills buffer with results
        
        Log::debugln(F("Serial received:"),buffer);
        
        buffer[0]=0;
        bufferSize=0;
      }
    }
  }
}
void listenEthernet()
{
  EthernetClient client = server.available();
  client.setTimeout(10000);

  char endChar = '\n';
  char receivedText[MAXBUFFERSIZE] = ""; //safe to change char text[] = "" despite char* receivedText="";
  
  if (client) 
  {
    Log::debugln(F("Client connected"));
    while (client.connected()) 
    {
      if (client.available()) 
      {
        char receivedChar = client.read();
        //Log::debugln(receivedChar);

        if (receivedChar==endChar)
        {
          //!!!This blocks current thread until command is done

          Log::debugln(F("Ethernet received:"), receivedText);
          parseCommand(receivedText); //Fills receivedText with results
          client.println(receivedText); // Sends the results to Ethernet client
          Log::debugln(F("Ethernet response:"), receivedText);
         
          // Clear received temp to be prepared to receive next command
          strcpy(receivedText, "\0");
        } 
        else
        {
          size_t len = strlen(receivedText);
          if (len < MAXBUFFERSIZE-1)
          {
            receivedText[len] = receivedChar;
            receivedText[len + 1] = '\0';
          }
          else
          {
            client.println("COMMAND_OVERFLOW"); // Send Error message
            Log::debugln(F("Max received message len reached."));
          }
        } 
      } 
    }

    Log::debugln(F("CLOSE CONNECTION")); 

    client.stop();
  }
  else
  {
    Log::debugln(F("No client, server stopped"));
  }  
}
void serverThreadCallback()
{
    if(useEthernet()){
      listenEthernet();
    }
    else{
      listenSerial();
    }
}
bool ConnectToServer(const byte* ip, const int port)
{
  if(arduinoClient){ 
    if(!arduinoClient.connected()){
      arduinoClient.stop();

      Log::debugln(F("MON: Reconnecting..."));

      if (arduinoClient.connect(ip, port)) {    
        return arduinoClient.connected();
      }
      else{
        return false;
      }
    }
    else{
      return true;
    } 
  }
  else{
    Log::debugln(F("MON: Connecting..."));

    arduinoClient.connect(ip, port);
    return arduinoClient.connected();
  }
}
void clientThreadCallback()
{
  byte i = 0;
  while(!(arduinoClient.connected()) && (i < 2))
  {
    ConnectToServer(serverIp, serverPort);
    i++;
  }
  if(!arduinoClient.connected())
  {
    Log::debugln(F("MON: Check connection to gateway."));

    if(!ConnectToServer(gateway, 80))
    {
      Log::debugln(F("MON: Cannot connect, reinitialize ethernet."));

      Ethernet.begin(mac, ip, dns, gateway, subnet);
      server.begin();
      Log::debugln(F("Server started"));
    }
    else
    {
      ConnectToServer(serverIp, serverPort);
    }
  } 

  if(arduinoClient.connected())
  {
    // Send status all the time
    Log::debugln(F("MON: Sending status from ..."), Ethernet.localIP());

    byte state = 0;
    if(isRestarted)
    { 
      state = 3;
    }
    // Send the state of the pins
    int digitalPinStates[14];
    for(byte i=0;i<14;i++)
    {
        digitalPinStates[i] = digitalRead(i);
    }
    constructPinStatesJSON(arduinoName, state, 0, digitalPinStates, 14, arduinoClient);

    int analogPinStates[6];
    for(byte i=0;i<=5;i++)
    {
        analogPinStates[i] = analogRead(i+14);
    }
    
    //--------DEVICE SPECIFIC---------------------------
    analogPinStates[1] = f1();
    //--------DEVICE SPECIFIC---------------------------

    constructPinStatesJSON(arduinoName, state, 1, analogPinStates, 6, arduinoClient);
  
    isRestarted=false;
    
    sendToServer("END");
  }
}

void setupThread()
{
  // serverThread.onRun(serverThreadCallback);
	// serverThread.setInterval(1000);

  // clientThread.onRun(clientThreadCallback);
	// clientThread.setInterval(5000);

  // threadsController.add(&serverThread);
  // threadsController.add(&clientThread);
}
void setup() {
  delay(250);
  isRestarted=true;

  Serial.begin(9600);
  Log::debugln(F("Entering Setup"));

  Log::debugln(F("FreeMem:"), freeMemory());
  
  delay(1000);
  
  // Sensor initialization
  #ifdef HASCURRENT
    // Sensor initialization
    calibrateCurrentSensor();
  #endif
  #ifdef HASTEMP
    dht.begin();
  #endif
  // Sensor initialization

  setupPins();
  initializePins();

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);

  delay(1000);
  Log::debugln(F("My IP address: "), Ethernet.localIP());
  
  server.begin();
  Serial.println(F("Server started"));

  setupThread();
}
void loop() {
  
  delay(100);
  
  // Receive commands
  serverThreadCallback();
  delay(100);

  if(noLoopRuns < 5){
    noLoopRuns++;
  }
  else{
    // send pin status
    clientThreadCallback();
    noLoopRuns = 0;
  }

  // Send ImAlive to WatchDog
  Log::debugln(F("I'm alive !"));

  digitalWrite(WatchDog, digitalRead(WatchDog)==0?1:0);
}
