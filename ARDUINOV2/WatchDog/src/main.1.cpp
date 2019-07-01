// #include <Arduino.h>

// byte resetSelf = 13;
// byte clientIN1 = 2;
// byte clientOUT1 = 3;
// int clientState1 = 0;
// unsigned long lastChangeTime1;

// void setup() {
//   Serial.begin(9600);
//   Serial.println("Entering Setup");

//   pinMode(resetSelf, OUTPUT);
//   digitalWrite(resetSelf, 1);
  
//   pinMode(clientIN1, INPUT_PULLUP);
//   pinMode(clientOUT1, OUTPUT);
//   digitalWrite(clientOUT1, 1);
// }

// void loop() {
//   delay(1000);

//   int currentState = digitalRead(clientIN1);
//   if(currentState==clientState1)
//   {
//     if(millis() - lastChangeTime1 > 10*pow(10, 3))
//     {
//       Serial.println("reset");
//       // reset client
//       digitalWrite(clientOUT1, 0);
//       delay(500);
//       digitalWrite(clientOUT1, 1);
//       lastChangeTime1 = millis();
//     }

//     Serial.print("same");Serial.print(currentState);Serial.println(clientState1);

//   } 
//   if(currentState!=clientState1)
//   {
//     Serial.print("change");Serial.print(currentState);Serial.println(clientState1);

//     clientState1 = digitalRead(clientIN1);
//     lastChangeTime1 = millis();
//   }
  
//   // Reset self if millis > 1 day - is not working this way
//   if(millis() > 24*3600*pow(10, 3))
//   {
//     digitalWrite(resetSelf,0);
//   }
// }
