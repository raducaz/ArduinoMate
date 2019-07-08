#include <Arduino.h>

// This is the matrix of clients with their states
// clients[0] = [0,1234234] - client on pin 2 is 0 from 1234234 timestamp
// clients[1] = [0,1234234] - client on pin 4 is 0 from 1234234 timestamp
unsigned long clients[6][2];

void setup() {
  Serial.begin(9600);
  Serial.println("Entering Setup");

  pinMode(resetSelf, OUTPUT);
  digitalWrite(resetSelf, 1);
  
  for(byte b=0;b<6;b++)
  {
    pinMode(2*(b+1), INPUT_PULLUP);
    pinMode(2*(b+1)+1, OUTPUT);
    digitalWrite(2*(b+1)+1, 1);
  }
}
void(* resetFunc) (void) = 0;

void loop() {
  delay(1000);

  for(byte b=0;b<6;b++)
  {
    Serial.println(b);

    byte clientIN = 2*(b+1);
    byte clientOUT = clientIN+1;
    int currentState = digitalRead(clientIN);
    if(currentState==clients[b][0])
    {
      if(millis() - clients[b][1] > 10*pow(10, 3))
      {
        Serial.println("reset");
        // reset client
        digitalWrite(clientOUT, 0);
        delay(500);
        digitalWrite(clientOUT, 1);
        clients[b][1] = millis();
      }

      Serial.print("same");Serial.print(currentState);Serial.println(clients[b][0]);

    } 
    if(currentState!=clients[b][0])
    {
      Serial.print("change");Serial.print(currentState);Serial.println(clients[b][0]);

      clients[b][0] = digitalRead(clientIN);
      clients[b][1] = millis();
    }
  }
  
  // Reset self if millis > 1 day - is not working this way
  if(millis() > (unsigned long)24*3600*pow(10, 3))
  {
    Serial.println("Self reset");
    delay(100);
    
    resetFunc();
  }
}
