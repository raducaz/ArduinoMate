#include <appendCommand.h>

char *trim(char *str)
{
    size_t len = 0;
    char *frontp = str;
    char *endp = NULL;

    if( str == NULL ) { return NULL; }
    if( str[0] == '\0' ) { return str; }

    len = strlen(str);
    endp = str + len;

    /* Move the front and back pointers to address the first non-whitespace
     * characters from each end.
     */
    while( isspace((unsigned char) *frontp) ) { ++frontp; }
    if( endp != frontp )
    {
        while( isspace((unsigned char) *(--endp)) && endp != frontp ) {}
    }

    if( str + len - 1 != endp )
            *(endp + 1) = '\0';
    else if( frontp != str &&  endp == frontp )
            *str = '\0';

    /* Shift the string so that it starts at str so that if it's dynamically
     * allocated, we can still free it on the returned pointer.  Note the reuse
     * of endp to mean the front of the string buffer now.
     */
    endp = str;
    if( frontp != str )
    {
            while( *frontp ) { *endp++ = *frontp++; }
            *endp = '\0';
    }
    return str;
}
bool appendCmdResult(char* res, char* cmd, char* value)
{
  Log::debugln(F("FreeMem:"), freeMemory());

  size_t resLen = strlen(res);
  size_t valLen = strlen(value);
  size_t cmdLen = strlen(cmd);

  if(resLen + cmdLen + valLen + 3 > MAXBUFFERSIZE)
  {
    strcpy(res, "RESPONSE_OVERFLOW");
    return false;
  }
  else
  {
    if(resLen>1){
      res[resLen] = '|';
      res[resLen+1] = '\0';
    }
    strcat(res, cmd); //append cmd in res 
    resLen = strlen(res);
    res[resLen] = ':';
    res[resLen+1] = '\0';
    
    strcat(res, value);
  }
  Log::debugln(F("FreeMem:"), freeMemory());

  return true;
}
bool appendCmdResult(char* res, char* cmd, int value)
{
  char buffer [5]; //max value is 4 chrs
  itoa(value, buffer, 10);
  return appendCmdResult(res, cmd, buffer);
}
bool appendCmdResult(char* res, char* cmd, float value)
{
  char buffer[100]; 
  dtostrf(value, '.', 2, buffer);
  trim(buffer);
  //ftoa(value, buffer, 2);
  return appendCmdResult(res, cmd, buffer);
}