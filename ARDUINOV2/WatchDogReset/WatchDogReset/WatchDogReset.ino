void setup() {
  for(byte b=0;b<6;b++)
  {
    pinMode(2*(b+1), INPUT_PULLUP);
    pinMode(2*(b+1)+1, OUTPUT);
    digitalWrite(2*(b+1)+1, 1);
  }

}

void loop() {
  // put your main code here, to run repeatedly:

}
