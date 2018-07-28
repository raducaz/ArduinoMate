#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

//#include <TimerOne.h>

#include <SPI.h>

// Satisfy IDE, which only needs to see the include statment in the ino.
#ifdef dobogusinclude
#include <spi4teensy3.h>
#endif

ThreadController threadsController = ThreadController();

void startThreadsController()
{
//  // Is best practice to start the threadController from a Timer interrupt so we avoid blocking the main thread
//  Timer1.stop();
//
//  Timer1.initialize(500); // in micro second (us)
//  Timer1.attachInterrupt(starterTimerCallback);
//  Timer1.start();
}

void starterTimerCallback(){
  threadsController.run();
  //Timer1.stop();
}
class MyTcpServerThread: public Thread
{
    // Function executed on thread execution
    void run(){

Serial.println("run");

    wait(2000);

    runned();

  }

void wait(int msInterval)
{
    unsigned long waitStart = millis();
    Serial.print("wait");Serial.println(msInterval);Serial.println(waitStart);

    unsigned long current = millis();
    while((current - waitStart)< msInterval) 
    {
      current = millis();
      Serial.print("Current:");Serial.println(current);
    }; // wait until 
    
    Serial.print("done wait");Serial.println(millis());
}  

};

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(500); // in ms
  threadsController.add(&tcpServerThread);
}

void setup() {
  Serial.begin(9600);

  setupTcpServerThread();
  // Do not start througth Timer because it resets the millis to 500 (Timer initializer value)
  //startThreadsController();
}

void loop() {
  delay(500);
  threadsController.run();
  delay(500);
}

