#include <Arduino.h>
#include <Thread.h>
#include <ThreadController.h>

#include <ArduinoJson.h>

#include <Ethernet.h>
#include <EthernetClient.h>
#include <EthernetServer.h>
#include "globals.h"
#include <SPI.h>
#include <ACS712.h>
#include <configuration.cpp>
#include <tcpclient.h>
#include <tcpserver.h>
#include <executor.h>

volatile byte DeviceState = 0; // 0=READY,1=BUSY,2=ERROR

ThreadController threadsController = ThreadController();

// We have 30 amps version sensor connected to A5 pin of arduino
ACS712 sensor(ACS712_30A, A5);
float zeroCurrent = 0;

void calibrateCurrentSensor()
{
  // calibrate() method calibrates zero point of sensor,
  // It is not necessary, but may positively affect the accuracy
  // Ensure that no current flows through the sensor at this moment
  // If you are not sure that the current through the sensor will not leak during calibration - comment out this method
  sensor.calibrate();

  float c = 0;
  for(int i=0;i<10;i++)
  {
    c += sensor.getCurrentAC();
    delay(100);
  }
  zeroCurrent = c / 10;
}

void setupTcpServerThread()
{
  MyTcpServerThread tcpServerThread = MyTcpServerThread();
  // Set the interval the thread should run in loop
  tcpServerThread.setInterval(1000); // in ms
  threadsController.add(&tcpServerThread);

  MyMonitorTcpClientThread monitorTcpClientThread = MyMonitorTcpClientThread(ip, 
                                        mac, 
                                        serverIp, 
                                        serverPort, 
                                        gateway, dns, subnet);
  monitorTcpClientThread.setInterval(500); // in ms
  threadsController.add(&monitorTcpClientThread);
}

void setup() {
  // put your setup code here, to run once:

  Serial.begin(9600);
  Serial.println("Entering Setup");

  Configuration::setupPins();
  Configuration::initializePins();

  delay(1000);
  calibrateCurrentSensor();
  
  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip, dns, gateway, subnet);
  delay(1000);
  Serial.print("My IP address: ");
  Serial.println(Ethernet.localIP());
  
  setupTcpServerThread();
}

void loop() {
  // put your main code here, to run repeatedly:

  delay(500);
  //Start the Thread in loop
  threadsController.run();
  delay(500);
}
