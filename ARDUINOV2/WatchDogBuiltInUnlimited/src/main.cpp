#include <Arduino.h>
#include<avr/wdt.h> /* Header for watchdog timers in AVR */


unsigned long resetTime = 0;
#define TIMEOUTPERIOD 10000             // You can make this time as long as you want,
                                       // it's not limited to 8 seconds like the normal
                                       // watchdog
#define doggieTickle() resetTime = millis();  // This macro will reset the timer
void(* resetFunc) (void) = 0; //declare reset function @ address 0

void watchdogSetup()
{
cli();  // disable all interrupts
wdt_reset(); // reset the WDT timer
MCUSR &= ~(1<<WDRF);  // because the data sheet said to
/*
WDTCSR configuration:
WDIE = 1 :Interrupt Enable
WDE = 1  :Reset Enable - I won't be using this on the 2560
WDP3 = 0 :For 1000ms Time-out
WDP2 = 1 :bit pattern is 
WDP1 = 1 :0110  change this for a different = this is 1s
WDP0 = 0 :timeout period.
*/
// Enter Watchdog Configuration mode:
WDTCSR = (1<<WDCE) | (1<<WDE);
// Set Watchdog settings: interrupte enable, 0110 for timer
WDTCSR = (1<<WDIE) | (0<<WDP3) | (1<<WDP2) | (1<<WDP1) | (0<<WDP0);
sei();
}

ISR(WDT_vect) // Watchdog timer interrupt.
{ 
  if(millis() - resetTime > TIMEOUTPERIOD){
    resetFunc();     // This will call location zero and cause a reboot.
  }
  
}

void setup() {
  Serial.begin(9600); /* Define baud rate for serial communication */
  Serial.println("Watchdog Demo Starting");
  pinMode(13, OUTPUT);
  
  wdt_disable();  /* Disable the watchdog and wait for more than 2 seconds */
  delay(3000);  /* Done so that the Arduino doesn't keep resetting infinitely in case of wrong configuration */

  Serial.println("Dog setup");
  watchdogSetup();
  Serial.println("finished watchdog setup");  // just here for testing
}

void loop() {
  for(int i = 0; i<20; i++) /* Blink LED for some time */ 
  {
    Serial.print("blink");Serial.println(i);
    digitalWrite(13, HIGH);
    delay(100);
    digitalWrite(13, LOW);
    delay(100);
    
    doggieTickle();  /* Reset the watchdog */
  }
  
  Serial.println("Freeze!");
  while(1); /* Infinite loop. Will cause watchdog timeout and system reset. */
}
