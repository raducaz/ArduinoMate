#include <SPI.h>


void setup() {
  Serial.begin(9600);
  
  // OUTPUT PINS
  pinMode(7, INPUT_PULLUP); //Sets the pin to high

}

void loop() {

  Serial.println(digitalRead(7));
  //when connected to ground the pin will be LOW
}


