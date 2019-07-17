void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Entering Setup");
}
char *readInput(size_t size)
 {
    char *input;
    int   chr;
    input = malloc(size + 1);
    if (input == NULL)
        return NULL;
    
    int i = 0;
    while ((i < size) && ((chr = Serial.read()) != '\n') && (chr != EOF))
        input[i++] = chr;
    input[size] = '\0'; /* nul terminate the array, so it can be a string */
    return input;
 }
void loop() {
  // put your main code here, to run repeatedly:
  char *input;
 input = readInput(100);
 if (input == NULL)
     Serial.println("NULL");
 if(input[0]>0)
 {
  Serial.print("input:");Serial.println(input);
 }
 /* now you can free it */
 free(input);
}
