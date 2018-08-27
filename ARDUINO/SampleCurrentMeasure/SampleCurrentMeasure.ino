#include "ACS712.h"

/*
  This example shows how to measure the power consumption
  of devices in 230V electrical system
  or any other system with alternative current
*/

// We have 30 amps version sensor connected to A0 pin of arduino
// Replace with your version if necessary
ACS712 sensor(ACS712_30A, A5);
float initialCurrent = 0;

void setup() {
  Serial.begin(9600);

pinMode(3, OUTPUT);
digitalWrite(3,1);
pinMode(A5, INPUT);

  // calibrate() method calibrates zero point of sensor,
  // It is not necessary, but may positively affect the accuracy
  // Ensure that no current flows through the sensor at this moment
  // If you are not sure that the current through the sensor will not leak during calibration - comment out this method
  Serial.println("Calibrating... Ensure that no current flows through the sensor at this moment");
  for(int i=0;i<100;i++)
  {
    sensor.calibrate();
    delay(100);
  }
  Serial.print("Done! Initial current: ");

  float c = 0;
  for(int i=0;i<10;i++)
  {
    c += sensor.getCurrentAC();
    delay(100);
  }
  initialCurrent = c / 10;

  Serial.println(initialCurrent);
}

void loop() {
  // We use 230V because it is the common standard in European countries
  // Change to your local, if necessary
  float U = 230;

  // To measure current we need to know the frequency of current
  // By default 50Hz is used, but you can specify desired frequency
  // as first argument to getCurrentAC() method, if necessary
  float I = sensor.getCurrentAC() - initialCurrent;

  // To calculate the power we need voltage multiplied by current
  float P = U * I;

  Serial.println(String("I = ") + I + " A");
  Serial.println(String("P = ") + P + " Watts");

  delay(1000);

  if (Serial.available()!=0) {             //Wait for user input
  
          char receivedChar = Serial.read();
          Serial.println(receivedChar);
          if (receivedChar=='0')
          {
            digitalWrite(3,1);
          }else if(receivedChar=='1'){
            digitalWrite(3,0);
          }
  }
}
