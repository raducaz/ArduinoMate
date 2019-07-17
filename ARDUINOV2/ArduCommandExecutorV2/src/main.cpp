#include <Arduino.h>
#include <MemoryFree.h>

#include <math.h>
#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include "globals.h"
#include <SPI.h>
#include <ACS712.h>
#include <configuration.h>
#include "Log.h"
#include <ArduinoJson.h>

#include "Adafruit_Sensor.h"
#include "DHT.h"

#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321
DHT dht(Configuration::TemperatureSensor, DHTTYPE);

byte noLoopRuns = 0;
bool isRestarted = true;
const char PIN[4] = "pin";
const char NAME[5] = "name";
const char DEVICESTATE[6] = "state";
const char VALUE[6] =     "value";
const char FCTNAME[8] =   "fctName";
const char FCTSTATE[9] =  "fctState";
const char MSG[4] = "msg";
const char DIGITAL[12] = "digitalPins";
const char ANALOG[11] = "analogPins";

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A1);
float zeroCurrent = 0;

static const unsigned int MAXBUFFERSIZE = 250; //for input and output - same size

EthernetClient arduinoClient;
EthernetServer server = EthernetServer(arduinoPort);

// Reset function
void(* resetFunc) (void) = 0;

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
  zeroCurrent = c / 10.0;
}
void f0()
{
  resetFunc();
}
float f1()
{
  return sensor.getCurrentAC()-zeroCurrent;
}
int f2()
{
  // To ensure temp can be read using pins 0 and 1 need to Serial.end();
  float t = dht.readTemperature();
  
  // Check if any reads failed and exit early (to try again).
  if (isnan(t)) {
    return -100;
  }
  return (int)t;
}

// This is the callback for the Timer
// void timerCallback(){
// 	threadsController.run();
// }

void wait(unsigned int msInterval)
{
  if(msInterval>9000) msInterval = 9000; //limit wait to 9sec

  Log::debugln(F("wait for "),msInterval);

  unsigned long waitStart = millis();
  unsigned long current = millis();
  while((current - waitStart)< msInterval) 
  {
    current = millis();
  }; 
}  

void sendToServer(const char* msg)
{
  arduinoClient.println(msg);

  Log::debugln(msg);
}
void setDigitalPin(byte pin, byte state)
{
  Log::debugln(F("setPin:"),pin);
  Log::debugln(F(" to "),state);
  digitalWrite(pin, state);
}
void setAnalogPin(byte pin, int state)
{
  Log::debugln(F("setPin:"),pin);
  Log::debugln(F(" to "),state);
  analogWrite(pin, state);
}
void setDigitalPinTemp(byte pin, byte state, unsigned int interval)
{
  Log::debugln(F("setPinTemp:"),pin);
  Log::debugln(F(" to "),state);
  Log::debugln(F(" for "),interval);

  byte state1 = digitalRead(pin);
  digitalWrite(pin, state);
  wait(interval);
  digitalWrite(pin, state1);
}

void setAnalogPinTemp(byte pin, int state, unsigned int interval)
{
  Log::debugln(F("setPinTemp:"),pin);
  Log::debugln(F(" to "),state);
  Log::debugln(F(" for "), interval);

  int state1 = analogRead(pin);
  analogWrite(pin, state);
  wait(interval);
  analogWrite(pin, state1);
}

void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    const int* pinStates, byte size, 
    const char* msg)
  {
      StaticJsonBuffer<300> _buffer;
      
      Log::debugln(F("FreeMem:"), freeMemory());

      JsonObject& _root = _buffer.createObject();
      if(strcmp(msg,"") != 0)
          _root[MSG] = msg;
      _root[NAME] = deviceName;
      _root[DEVICESTATE] = deviceState;

      Log::debugln(F("FreeMem:"), freeMemory());

      JsonArray& psArr = _root.createNestedArray(pinType==0?DIGITAL:ANALOG);
      for(byte i=0;i<size;i++)
      {
          psArr.add(pinStates[i]);
      }

      Log::debugln(F("FreeMem:"), freeMemory());

      _root.printTo(arduinoClient);
      arduinoClient.println();

      Log::debugln(F("FreeMem:"), freeMemory());

      _root.printTo(Serial);
      Serial.println();

      Log::debugln(F("FreeMem:"), freeMemory());
  }
void constructPinStatesJSON(const char* deviceName,
    const byte deviceState,
    byte pinType, 
    int* pinStates, byte size)
{
    constructPinStatesJSON(deviceName, deviceState, pinType, pinStates, size, "");
}
bool appendCmdResult(char* res, char* cmd, char* value)
{
  // /*malloc*/
  // char* result;
  // char buffer [17]; //max int on 16bit processor
  // char* sVal = itoa(value, buffer, 10);
  // size_t resLen = strlen(res);
  // size_t cmdLen = strlen(cmd);
  // size_t valLen = strlen(sVal);
  // if((result = (char*) malloc(cmdLen+resLen+valLen+1+1+1)) != NULL)
  // {
  //   result[0]='\0';
  //   strcat(result,res);
  //   strcat(result,"|");
  //   strcat(result,cmd);
  //   strcat(result,":");
  //   strcat(result,sVal);
  // }
  // else
  // {
  //   Log::debugln("Error malloc");
  // }
  
  // free(res); //deallocate old pointer
  // res=NULL;
  // return result;
  // /*malloc*/

  Log::debugln(F("FreeMem:"), freeMemory());

  size_t resLen = strlen(res);
  size_t valLen = strlen(value);
  size_t cmdLen = strlen(cmd);

  if(resLen + cmdLen + valLen + 3 > MAXBUFFERSIZE)
  {
    strcpy(res, "RESPONSE_OVERFLOW");
    return false;
  }
  else
  {
    if(resLen>1){
      res[resLen] = '|';
      res[resLen+1] = '\0';
    }
    strcat(res, cmd); //append cmd in res 
    resLen = strlen(res);
    res[resLen] = ':';
    res[resLen+1] = '\0';
    
    strcat(res, value);
  }
  Log::debugln(F("FreeMem:"), freeMemory());

  return true;
}
bool appendCmdResult(char* res, char* cmd, int value)
{
  char buffer [5]; //max value is 4 chrs
  itoa(value, buffer, 10);
  return appendCmdResult(res, cmd, buffer);
}
// reverses a string 'str' of length 'len' 
void reverse(char *str, int len) 
{ 
    int i=0, j=len-1, temp; 
    while (i<j) 
    { 
        temp = str[i]; 
        str[i] = str[j]; 
        str[j] = temp; 
        i++; j--; 
    } 
} 
// Converts a given integer x to string str[].  d is the number 
 // of digits required in output. If d is more than the number 
 // of digits in x, then 0s are added at the beginning. 
int intToStr(int x, char str[], int d) 
{ 
    int i = 0; 
    while (x) 
    { 
        str[i++] = (x%10) + '0'; 
        x = x/10; 
    } 
  
    // If number of digits required is more, then 
    // add 0s at the beginning 
    while (i < d) 
        str[i++] = '0'; 
  
    reverse(str, i); 
    str[i] = '\0'; 
    return i; 
} 
// Converts a floating point number to string. 
void ftoaold(float n, char *res, int afterpoint) 
{ 
    // Extract integer part 
    int ipart = (int)n; 
  
    // Extract floating part 
    float fpart = n - (float)ipart; 
  
    // convert integer part to string 
    int i = intToStr(ipart, res, 0); 
  
    // check for display option after point 
    if (afterpoint != 0) 
    { 
        res[i] = '.';  // add dot 
  
        // Get the value of fraction part upto given no. 
        // of points after dot. The third parameter is needed 
        // to handle cases like 233.007 
        fpart = fpart * pow(10, afterpoint); 
  
        intToStr((int)fpart, res + i + 1, afterpoint); 
    } 
} 
bool appendCmdResult(char* res, char* cmd, float value)
{
  char buffer[10]; 
  //gcvt(value, 2, buffer);
  dtostrf(value, '.', 2, buffer);
  //ftoa()
  return appendCmdResult(res, cmd, buffer);
}
int getPin(const char* key)
{
  char sPin[4]="";
  bool isAnalogPin = (key[0]=='A'||key[0]=='a');
  
  byte i= 0; 
  byte index = (isAnalogPin ? 1 : 0);
  while(key[index+i]){
    sPin[i]=key[index+i];
    i++;
  }
  sPin[i] = '\0';
  
  int pinNo = atoi(sPin);
  //return isAnalogPin ? (pinNo<2 ? pinNo+16 : pinNo+18) : pinNo;
  return isAnalogPin ? pinNo+14 : pinNo; //A0 is 14, A1 is 15...
}
int getCmdParam(char* cmd, byte paramIndex, bool returnAsPin)
{
  int cmdLen = strlen(cmd);
  
  byte i = 1; //First char is cmd char followed by first argument =3:1:12
  byte index = 0;
  char res[cmdLen] = "";
  const char separator = ':';
  while(cmd[i]>0){

    if(index>paramIndex)
        break;

    if(cmd[i]==separator){
      index++;

    } else if(index==paramIndex){
      size_t len = strlen(res);
      res[len] = cmd[i];
      res[len + 1] = '\0';
    }

    i++;
  }

  int iRes = returnAsPin ? getPin(res) : atoi(res);
  Log::debugln(F("iRes:"),iRes);
  char buffer[6]=""; //can be !2000\0
  char* sRes = itoa(iRes,buffer,10);
  Log::debugln(F("sRes:"),sRes);
  Log::debugln(F("res:"),res);

  if(iRes==0 && strcmp(res,"0")!=0){
      return -1;
  }
  else{
    return iRes;
  }
    
  
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
    Log::debugln("Deserialize received message failed.");
    strcat(res, "PARSE_ERROR]");
  }

  strcpy(plainJson, res);

  Log::debugln(F("FreeMem:"), freeMemory());

}
void listenSerial()
{
  char buffer[MAXBUFFERSIZE] = ""; 
  unsigned int bufferSize = 0;

  Log::debugln("Listen Serial");

  char endChar = '\n';
  const byte SerialSize = 64; // This is Serial.read limit
  
  if(Serial){

    if (Serial.available()!=0) {
      Log::debugln("Serial available");

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

      //TODO: Test - this executes actual commands and blocks the thread until done
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
    Log::debugln("Client connected");
    while (client.connected()) 
    {
      if (client.available()) 
      {
        char receivedChar = client.read();
        //Log::debugln(receivedChar);

        if (receivedChar==endChar)
        {
          //!!!This blocks current thread until command is done
          parseCommand(receivedText); //Fills receivedText with results
          client.println(receivedText); // Sends the results to Ethernet client

          Log::debugln(F("Ethernet received:"), receivedText);
         
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
            Log::debugln("Max received message len reached.");
          }
        } 
      } 
    }

    Log::debugln("CLOSE CONNECTION"); 

    client.stop();
  }
  else
  {
    Log::debugln("No client, server stopped");
  }  
}
void serverThreadCallback()
{
    if(Configuration::useEthernet()){
      listenEthernet();
    }
    else{
      listenSerial();
    }
}
bool ConnectToServer(const byte* ip, const int port)
{
  if(arduinoClient)
  { 
    if(!arduinoClient.connected())
    {
      arduinoClient.stop();
      
      Log::debugln("MON: Reconnecting...");

      if (arduinoClient.connect(ip, port)) {    
        return arduinoClient.connected();
      }
      else
      {
        return false;
      }
    }
    else
    {
      return true;
    }
    
  }
  else
  {
    Log::debugln("MON: Connecting...");

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
    Log::debugln("MON: Check connection to gateway.");

    if(!ConnectToServer(gateway, 80))
    {
      Log::debugln("MON: Cannot connect, reinitialize ethernet.");

      Ethernet.begin(mac, ip, dns, gateway, subnet);
    }
  } else
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
    constructPinStatesJSON(arduinoName, state, 0, digitalPinStates, 14);

    int analogPinStates[6];
    for(byte i=0;i<=5;i++)
    {
        analogPinStates[i] = analogRead(i+14);
    }
    
    //--------DEVICE SPECIFIC---------------------------
    analogPinStates[1] = f1();
    //--------DEVICE SPECIFIC---------------------------

    constructPinStatesJSON(arduinoName, state, 1, analogPinStates, 6);
  
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

  Serial.begin(9600);
  Log::logln("Entering Setup");

  Log::debugln(F("FreeMem:"), freeMemory());
  
  delay(1000);
  // Sensor initialization
  calibrateCurrentSensor();
  dht.begin();
  // Sensor initialization

  Configuration::setupPins();
  Configuration::initializePins();

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);

  delay(1000);
  Log::debugln(F("My IP address: "), Ethernet.localIP());
  
  server.begin();
  Log::debugln(F("Server started"));

  setupThread();
}
void loop() {
  
  delay(500);
  
  // Receive commands
  serverThreadCallback();
  delay(500);

  if(noLoopRuns < 5)
  {
    noLoopRuns++;
  }
  else
  {
    // send pin status
    clientThreadCallback();
    noLoopRuns = 0;
  }

  // Send ImAlive to WatchDog
  Log::logln("I'm alive !");

  digitalWrite(Configuration::WatchDog, digitalRead(Configuration::WatchDog)==0?1:0);
  delay(500);
  Serial.println(f2());

  // // Reading temperature or humidity takes about 250 milliseconds!
  // // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  // float h = dht.readHumidity();
  // // Read temperature as Celsius (the default)
  // float t = dht.readTemperature();
  // // Read temperature as Fahrenheit (isFahrenheit = true)
  // float f = dht.readTemperature(true);

  // // // Check if any reads failed and exit early (to try again).
  // if (isnan(h) || isnan(t) || isnan(f)) {
  //   Serial.println(F("Failed to read from DHT sensor!"));
  //   return;
  // }

  // // Compute heat index in Fahrenheit (the default)
  // float hif = dht.computeHeatIndex(f, h);
  // // Compute heat index in Celsius (isFahreheit = false)
  // float hic = dht.computeHeatIndex(t, h, false);

  // Serial.print(F("Humidity: "));
  // Serial.print(h);
  // Serial.print(F("%  Temperature: "));
  // Serial.print(t);
  // Serial.print(F("°C "));
  // Serial.print(f);
  // Serial.print(F("°F  Heat index: "));
  // Serial.print(hic);
  // Serial.print(F("°C "));
  // Serial.print(hif);
  // Serial.println(F("°F"));

}
