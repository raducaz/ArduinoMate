// #ifndef tcpclient_h
// #define tcpclient_h

// #include <Arduino.h>
// #include <Thread.h>
// #include <Ethernet.h>
// #include <EthernetClient.h>
// #include <SPI.h>
// #include <ACS712.h>

// class MyMonitorTcpClientThread: public Thread
// {
//   EthernetClient arduinoClient;

//   byte* mac;
//   const byte* ip;

//   byte* serverIp;
//   int serverPort;
//   byte* gateway;
//   byte* dns;
//   byte* subnet;

//   float zeroCurrent;

//   public: MyMonitorTcpClientThread(
//                                   const byte* ip, 
//                                   byte* mac, 
//                                   byte* serverIp, 
//                                   int serverPort, 
//                                   byte* gateway, byte* dns, byte* subnet,
//                                   float zeroCurrent
//                                   );

//   public: boolean ConnectToServer(const byte* ip, const int port);
  
//   private: void run();
// };
// #endif