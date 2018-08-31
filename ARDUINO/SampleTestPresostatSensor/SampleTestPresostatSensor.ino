#include <SPI.h>
#define IN A0
#define OUT A4

void setup() {
  Serial.begin(9600);
  
  // OUTPUT PINS
  //pinMode(7, INPUT_PULLUP); //Sets the pin to high

  pinMode(OUT, OUTPUT);
  pinMode(IN, INPUT_PULLUP);
}
bool probe;
void loop() {

  //Serial.println(digitalRead(7));
  //when connected to ground the pin will be LOW

if (Serial.available()!=0) {             //Wait for user input
  char receivedChar = Serial.read();
  if (receivedChar=='1')
  {
    probe = 1;
    Serial.println("set probe 1");
  }
  else
  {
    if(receivedChar != '\n')
    {
      probe = 0;
      Serial.println("set probe 0");
    }
  }
}

if(probe)
{
  digitalWrite(OUT, LOW);
  Serial.print("Probe");Serial.println(digitalRead(IN));
  //When close circuit -> IN = 0
  //When open circuit -> IN = 1
}
else
{
  digitalWrite(OUT, HIGH);
  Serial.print("Probe");Serial.println(digitalRead(IN));
  //When close or open circuit -> IN = 1
}
delay(500);
//Serial.println(digitalRead(A4));
}


