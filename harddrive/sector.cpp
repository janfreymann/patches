/* -----------------
  Jan Freymann
  October 2013
  
  http://cpsnd.wordpress.com
*/ -----------------

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <iostream>
#include <process.h>
#include <string.h>

using namespace std;

//global variables:

const int velocityScale = 4;
int currentTick;
int pattern[16];

void tickleHD(int count);

short ReadSect
       (const char *_dsk,    // disk to access
       char *&_buff,         // buffer where sector will be stored
       unsigned int _nsect   // sector number, starting with 0
       )
{
DWORD dwRead;   
HANDLE hDisk=CreateFile(_dsk,GENERIC_READ,FILE_SHARE_VALID_FLAGS,0,OPEN_EXISTING,0,0);
if(hDisk==INVALID_HANDLE_VALUE) // this may happen if another program is already reading from disk
  {  
     CloseHandle(hDisk);
     cout << "Invalid disk handle value" << endl;
     return 1;
  }
SetFilePointer(hDisk,_nsect*512,0,FILE_BEGIN); // which sector to read

ReadFile(hDisk,_buff,512,&dwRead,0);  // read sector
CloseHandle(hDisk);
return 0;
}

void induceActivity(void* velocity) {
     int vel = *(int*)velocity;
     
//     cout << "Inducing activity " << vel << endl;
     
     tickleHD(vel * velocityScale);
     
     _endthread();
}

void tickleHD(int count) {
    char * drv="\\\\.\\E:"; 
    char *buff=new char[512];
    for(int sector = 0; sector < count; sector++) {
               int k  = int(rand()) % 20000; 
               ReadSect(drv,buff,k);
    }
}

void changePattern(int indx, int vel) {
     if(pattern[indx] == vel) pattern[indx] = 0;
     else { pattern[indx] = vel; }
}

void printPattern() {
     for(int i = 0; i < 16; i++) {
             if(i % 4 == 0) cout << '|';
             if(pattern[i] == 5) cout << 'o'; 
             else if(pattern[i] == 10) cout << 'O';
             else  cout << '.'; 
     }
     cout << endl;
}

void getInput(void* dummy) {
     char input[60];
     while(1) {
              cin >> input;
              for(int k = 0; k < strlen(input); k++) {
                      switch(input[k]) {
                          case 'q': changePattern(0, 5); break;
                          case 'w': changePattern(1, 5); break;
                          case 'e': changePattern(2, 5); break;                          
                          case 'r': changePattern(3, 5); break;                          
                          case 't': changePattern(4, 5); break;                          
                          case 'z': changePattern(5, 5); break;
                          case 'u': changePattern(6, 5); break;
                          case 'i': changePattern(7, 5); break;                          
                          case 'a': changePattern(8, 5); break;                          
                          case 's': changePattern(9, 5); break;                          
                          case 'd': changePattern(10, 5); break;
                          case 'f': changePattern(11, 5); break;
                          case 'g': changePattern(12, 5); break;                          
                          case 'h': changePattern(13, 5); break;                          
                          case 'j': changePattern(14, 5); break;                                                                              
                          case 'k': changePattern(15, 5); break;
                          case 'Q': changePattern(0, 10); break;
                          case 'W': changePattern(1, 10); break;
                          case 'E': changePattern(2, 10); break;                          
                          case 'R': changePattern(3, 10); break;                          
                          case 'T': changePattern(4, 10); break;                          
                          case 'Z': changePattern(5, 10); break;
                          case 'U': changePattern(6, 10); break;
                          case 'I': changePattern(7, 10); break;                          
                          case 'A': changePattern(8, 10); break;                          
                          case 'S': changePattern(9, 10); break;                          
                          case 'D': changePattern(10, 10); break;
                          case 'F': changePattern(11, 10); break;
                          case 'G': changePattern(12, 10); break;                          
                          case 'H': changePattern(13, 10); break;                          
                          case 'J': changePattern(14, 10); break;                                                                              
                          case 'K': changePattern(15, 10); break;                          
                      }
              }
              printPattern();
     }
}

int main()
{
    srand(time(NULL));
    
    for(int i = 0; i < 16; i++) {
            pattern[i] = 0;
    }
    
    cout << "Welcome!\n";
    
    int dummy = 0;
    _beginthread(getInput, 0, (void*)&dummy);
    
    int clock = 0;
    
    while(1) {
             Sleep(125); //sleep is in milliseconds here
             if(pattern[clock] > 0) {
                          _beginthread(induceActivity, 0, (void*)&pattern[clock]);
             }
             
             clock = (clock + 1) % 16;
    }
}
