
#ifdef __arm__
// should use uinstd.h to define sbrk but Due causes a conflict
extern "C" char* sbrk(int incr);
#else  // __ARM__
extern char *__brkval;
#endif  // __arm__

int freeMemory() {
  char top;
#ifdef __arm__
  return &top - reinterpret_cast<char*>(sbrk(0));
#elif defined(CORE_TEENSY) || (ARDUINO > 103 && ARDUINO != 151)
  return &top - __brkval;
#else  // __arm__
  return __brkval ? &top - __brkval : &top - __malloc_heap_start;
#endif  // __arm__
}

char* appendCmdResult(char* res, char* cmd, int value)
{
  Serial.println(freeMemory());
  char* result;
  char buffer [17]; //max int on 16bit processor
  char* sVal = itoa(value, buffer, 10);
  size_t resLen = strlen(res);
  size_t cmdLen = strlen(cmd);
  size_t valLen = strlen(sVal);
  if((result = (char*) malloc(cmdLen+resLen+valLen+1+1+1)) != NULL)
  {
    result[0]='\0';
    strcat(result,res);
    strcat(result,"|");
    strcat(result,cmd);
    strcat(result,":");
    strcat(result,sVal);
  }
  else
  {
    Serial.println("Error malloc");
  }
  
  free(res); //deallocate old pointer

  Serial.println(freeMemory());
  return result;
}
char* parseCommand(char* plainJson)
{
  char* res;
  res[0]='\0';

  int i = 0;
  while(i<2)
  {
    res = appendCmdResult(res, plainJson, i);
    i++;
  }
 
  return res;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Entering Setup");
}

void loop() {
  // put your main code here, to run repeatedly:
  char cmd[] = "aaa";
  char* result = parseCommand(cmd);
  Serial.print("Res:");Serial.println(result);
  free(result);
}
