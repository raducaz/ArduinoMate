#include <executor.h>

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