#include <Arduino.h>

void setup() {
  pinMode(13, OUTPUT);
  digitalWrite(13, 1);

  Serial.begin(9600);

  Serial.println("Entering setup");
}

void loop() {

  // Reset self if millis > 10 sec
  if(millis() > 10*pow(10, 3))
  {
    Serial.println("Try reset");
    digitalWrite(13,0);
    delay(100);
    digitalWrite(13,1);
    
    delay(2000);
  }
}