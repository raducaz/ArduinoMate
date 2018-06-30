#include <SPI.h>
#include <Ethernet.h>

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network:
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDA, 0x02 };
IPAddress ip(191,11,1,2); //<<< ENTER YOUR IP ADDRESS HERE!!!

EthernetServer server(80);

const int ContactGenerator = 2; // controleaza releul pentru contact generator (default CUPLAT - trebuie DECUPLAT pentru functionare)
const int ContactRetea220V = 3; // controleaza releul porneste priza de 220V (default DECUPLAT - trebuie CUPLAT pentru functionare pompa)- ATENTIE PERICOL DE ELECTROCUTARE !!!!

const int ContactDemaror12V = 4; // controleaza releul de 12V pentru contact demaror (default DECUPLAT - trebuie CUPLAT pentru demarare) - ATENTIE CONTACTUL NU TREBUIE SA DUREZE

// Atentie, default Borna rosie = -, Borna neagra = -; Daca se cupleaza ambele relee ambele borne vor fi pe + !!!
const int ActuatorNormal = 5; // (fir portocaliu) controleaza releul 1 actuator (contact + la +) => Borna rosie = +, Borna neagra = -
const int ActuatorInversat = 6; // (fir mov) controleaza releul 2 actuator (contact + la -) => Borna neagra = +, Borna rosie = -

//const int ButonNegru = 9;// controleaza pornirea demararii generatorului
//const int ButonAlb = 8; // controleaza oprirea generatorului 

void setup() {
  //Serial.begin(9600);
  
  // OUTPUT PINS
  pinMode(ContactGenerator, OUTPUT);
  pinMode(ActuatorNormal, OUTPUT);
  pinMode(ActuatorInversat, OUTPUT);
  pinMode(ContactRetea220V, OUTPUT);
  pinMode(ContactDemaror12V, OUTPUT);
  
  digitalWrite(ContactGenerator, LOW);
  digitalWrite(ActuatorNormal, HIGH);
  digitalWrite(ActuatorInversat, HIGH);
  digitalWrite(ContactRetea220V, HIGH);
  digitalWrite(ContactDemaror12V, LOW);

  // start the Ethernet connection and the server:
  Ethernet.begin(mac, ip);
  server.begin();

}

bool cycleDone = true;
bool generatorPornit = false;
void loop() {

  // listen for incoming clients
  EthernetClient client = server.available();
  if (client) {
    // an http request ends with a blank line
    String receivedText = "";
    while (client.connected()) {
      if (client.available()) {
        
        char receivedChar = client.read();
        if (receivedChar != '\n' && receivedChar != '\r')
        {
          receivedText.concat(receivedChar);
        }
        else if (receivedChar != '\n')
        {
          client.println("COMMAND RECEIVED:" + receivedText);
          break;
        }
      }
      
    }
    
    if(receivedText == "START")
    {
      if(generatorPornit == false)
      {
        client.println("INTO: PORNIRE GENERATOR ...");
        pornire(client);
      }
      else
      {
        client.println("EROARE: GENERATOR DEJA PORNIT !");
      }
    }
    else if(receivedText == "STOP")
    {
      if(generatorPornit == false)
      {
        client.println("INTO: GENERATOR DEJA OPRIT ... INITIERE OPRIRE REPETATA ...");
      }
      oprire(client);
    }
    else if(receivedText == "CLOSE")
    {
      client.println("CLOSING CONNECTION....");
      delay(1);
      client.stop();
    }
    else
    {
      client.println("COMANDA NERECUNOSCUTA:" + receivedText);
      client.println("COMENZI RECUNOSCUTE:");
      client.println("START: Porneste ciclul de pornire generator.");
      client.println("STOP: Opreste generatorul.");
      client.println("CLOSE: Inchide conexiunea la terminal.");
    }
    
    delay(1);
  }
}

void sendMessageToClient(EthernetClient client, String message)
{
  if(client)
    client.println("INFO:" + message);
}

void pornire(EthernetClient client)
{
  if(!generatorPornit)
  {
    // Tras soc
    digitalWrite(ActuatorNormal, LOW); // Cuplare
    digitalWrite(ActuatorInversat, HIGH); // DECUPLARE
    sendMessageToClient(client, "Actuator - PORNIRE INAINTE");
    delay(500);
    digitalWrite(ActuatorNormal, HIGH);// DECUPLARE
    digitalWrite(ActuatorInversat, HIGH);// DECUPLARE
    sendMessageToClient(client, "Actuator - OPRIT");
    delay(2000);

    // Punere contact
    digitalWrite(ContactGenerator, LOW); //CUPLARE releu = intrerupere circuit contact pentru pornire generator
    sendMessageToClient(client, "Contact - ON");
    delay(2000);

    // Contact motor
    digitalWrite(ContactDemaror12V, HIGH); // CUPLARE
    sendMessageToClient(client, "ContactDemaror12V - PORNIRE");
    delay(4000);
    digitalWrite(ContactDemaror12V, LOW); // DECUPLARE
    sendMessageToClient(client, "ContactDemaror12V - OPRIRE");
    delay(4000);

    // Scoatere soc
    digitalWrite(ActuatorNormal, HIGH); // DECUPLARE
    digitalWrite(ActuatorInversat, LOW); // CUPLARE 
    sendMessageToClient(client, "Actuator - PORNIRE INAPOI");
    delay(500);
    digitalWrite(ActuatorNormal, HIGH); // DECUPLARE
    digitalWrite(ActuatorInversat, HIGH); // DECUPLARE
    sendMessageToClient(client, "Actuator - OPRIT");

    //TODO: Testare prezenta curent 220 - trebuie consumator pe priza
    // In cazul in care nu este curent se initiaza procedura de inchidere generator

    // Cuplare priza 220V iesire
    delay(2000);
    digitalWrite(ContactRetea220V, LOW); // CUPLARE
    sendMessageToClient(client, "ContactRetea220V - PORNIRE");    
  }

  generatorPornit = true;
  sendMessageToClient(client, "GENERATOR - PORNIT !!!");
}

void oprire(EthernetClient client)
{
    digitalWrite(ContactRetea220V, HIGH); //DECUPLARE
    sendMessageToClient(client, "ContactRetea220V - OPRIT");
    delay(2000);
    
    // Punere contact
    digitalWrite(ContactGenerator, HIGH); //DECUPLARE releu = inchidere circuit contact pentru oprire generator
    sendMessageToClient(client, "Contact - OFF");
    delay(2000);
    sendMessageToClient(client, "ContactGenerator - OPRIT");

    generatorPornit = false;
    sendMessageToClient(client, "GENERATOR - OPRIT !!!");

}


