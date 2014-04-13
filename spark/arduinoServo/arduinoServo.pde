#include <Servo.h> 

Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 
void setup() 
{ 
  myservo.attach(9);  // attaches the servo on pin 9 to the servo object 
} 
 
 
void openDoor() 
{ 
  myservo.write(150);
  delay(1000);
  myservo.write(180);
  delay(1000);
  myservo.write(220);
  delay(3000);
  myservo.write(150);
} 
