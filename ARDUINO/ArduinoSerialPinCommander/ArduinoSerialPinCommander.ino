
#include <SPI.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif


void setup() {
  Serial.begin(9600);

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

  Serial.println("2=1 to set pin 2 to 1; ls to list all pins"); //Prompt User for input
}

char cmdChar = '\0';
const byte SIZE = 10;
char receivedText[SIZE] = ""; 
int pin = 0;
int value = 0;

void loop() {
  
  if (Serial.available()!=0) {             //Wait for user input
  
          char receivedChar = Serial.read();
          
          if (receivedChar=='\n')
          {
            if(strncmp(receivedText, "ls", 2)==0)
            {
              for(int i=0;i<=13;i++)
              {
                Serial.print(i);Serial.print("=");Serial.println(digitalRead(i));
              }
            }
            else if(cmdChar == '=')
            {
              value = atoi(receivedText);
            
              Serial.print("set pin:");Serial.print(pin);
              Serial.print(" to value:");Serial.println(value);
            
              digitalWrite(pin,value);
            }
            else if(cmdChar == '?')
            {
              Serial.print("pin:");Serial.print(pin);
              Serial.print(" is:");Serial.println(digitalRead(pin));
            }
            else
            {
              Serial.println("no command");
            }
            
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

