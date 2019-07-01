void setup() {
  digitalWrite(7, 1);
  pinMode(7,OUTPUT);
  
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Entering Setup");
}
void(* resetFunc) (void) = 0;

void loop() {
  // put your main code here, to run repeatedly:

delay(2000);
Serial.println("Running");
delay(2000);
Serial.println("Resetting");
delay(100);
resetFunc();
}
