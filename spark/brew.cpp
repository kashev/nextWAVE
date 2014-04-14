//Device ID
//48ff70065067555028111587

//Access Token
//4348526a1c0932c678d6e971ce456b9d2ea4a1f5

#define DURATION	50000
#define NOTE_BF4	1073
#define NOTE_AF4	1205
#define NOTE_F4		1433
#define NOTE_EF5	804
#define NOTE_D5		852
#define PAUSE		50000
#define MAX_NOTES		11



Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 

int funkyTown[12] = {NOTE_BF4, NOTE_BF4, NOTE_AF4, NOTE_BF4, 
					PAUSE, NOTE_F4, PAUSE, NOTE_F4,
					NOTE_BF4, NOTE_EF5, NOTE_D5, NOTE_BF4};

int16_t audioPin = D6;
int16_t x,y = 0;



// Define the pins we're going to call pinMode on
int led = D0;  // You'll need to wire an LED to this one to see it blink.
int led2 = D7; // This one is the built-in tiny one to the right of the USB jack
volatile int flag = 0;
int flagvalue = 0;
int motorpin = A0;
volatile int open_door_flag = 0;

class elapsedMillis
{
private:
	unsigned long ms;
public:
	elapsedMillis(void) { ms = millis(); }
	elapsedMillis(unsigned long val) { ms = millis() - val; }
	elapsedMillis(const elapsedMillis &orig) { ms = orig.ms; }
	operator unsigned long () const { return millis() - ms; }
	elapsedMillis & operator = (const elapsedMillis &rhs) { ms = rhs.ms; return *this; }
	elapsedMillis & operator = (unsigned long val) { ms = millis() - val; return *this; }
	elapsedMillis & operator -= (unsigned long val)      { ms += val ; return *this; }
	elapsedMillis & operator += (unsigned long val)      { ms -= val ; return *this; }
	elapsedMillis operator - (int val) const           { elapsedMillis r(*this); r.ms += val; return r; }
	elapsedMillis operator - (unsigned int val) const  { elapsedMillis r(*this); r.ms += val; return r; }
	elapsedMillis operator - (long val) const          { elapsedMillis r(*this); r.ms += val; return r; }
	elapsedMillis operator - (unsigned long val) const { elapsedMillis r(*this); r.ms += val; return r; }
	elapsedMillis operator + (int val) const           { elapsedMillis r(*this); r.ms -= val; return r; }
	elapsedMillis operator + (unsigned int val) const  { elapsedMillis r(*this); r.ms -= val; return r; }
	elapsedMillis operator + (long val) const          { elapsedMillis r(*this); r.ms -= val; return r; }
	elapsedMillis operator + (unsigned long val) const { elapsedMillis r(*this); r.ms -= val; return r; }
};

class elapsedMicros
{
private:
	unsigned long us;
public:
	elapsedMicros(void) { us = micros(); }
	elapsedMicros(unsigned long val) { us = micros() - val; }
	elapsedMicros(const elapsedMicros &orig) { us = orig.us; }
	operator unsigned long () const { return micros() - us; }
	elapsedMicros & operator = (const elapsedMicros &rhs) { us = rhs.us; return *this; }
	elapsedMicros & operator = (unsigned long val) { us = micros() - val; return *this; }
	elapsedMicros & operator -= (unsigned long val)      { us += val ; return *this; }
	elapsedMicros & operator += (unsigned long val)      { us -= val ; return *this; }
	elapsedMicros operator - (int val) const           { elapsedMicros r(*this); r.us += val; return r; }
	elapsedMicros operator - (unsigned int val) const  { elapsedMicros r(*this); r.us += val; return r; }
	elapsedMicros operator - (long val) const          { elapsedMicros r(*this); r.us += val; return r; }
	elapsedMicros operator - (unsigned long val) const { elapsedMicros r(*this); r.us += val; return r; }
	elapsedMicros operator + (int val) const           { elapsedMicros r(*this); r.us -= val; return r; }
	elapsedMicros operator + (unsigned int val) const  { elapsedMicros r(*this); r.us -= val; return r; }
	elapsedMicros operator + (long val) const          { elapsedMicros r(*this); r.us -= val; return r; }
	elapsedMicros operator + (unsigned long val) const { elapsedMicros r(*this); r.us -= val; return r; }
};

int cook(String args)
{
  int value_time = atoi(args.c_str());
  flag = 1;
  flagvalue = value_time*1000;
  return value_time;

}

int stopcook(String args)
{

  flag = 0;
  flagvalue = 0;
  return 1;

}

int opendoor(String args)
{

  open_door_flag = 1;
  return 1;

}

void takeMeTo(int pin, int note){
	for(x = 0; x < (DURATION/(note*2)); x++) {
	    PIN_MAP[pin].gpio_peripheral->BSRR = PIN_MAP[pin].gpio_pin; // HIGH
	    delayMicroseconds(note);
	    PIN_MAP[pin].gpio_peripheral->BRR = PIN_MAP[pin].gpio_pin;  // LOW
	    delayMicroseconds(note);
	  }
	  y++;
	  if(y >= MAX_NOTES) y = 0;
	  delay(250);
}

void outputmusic()
{
    int i = 0;
    for (i = 0; i < 12; i++) {
    takeMeTo(audioPin, funkyTown[i]);
    }
}


void openDoor() 
{ 
  myservo.write(90);
  delay(1000);
  myservo.write(180);
  delay(1000);
  myservo.write(220);
  delay(1000);
  myservo.write(90);
} 

// This routine runs only once upon reset
void setup() {
  // Initialize D0 + D7 pin as output
  // It's important you do this here, inside the setup() function rather than outside it or in the loop function.
  myservo.attach(motorpin);
  pinMode(audioPin, OUTPUT);
  pinMode(led, OUTPUT);
  pinMode(led2, OUTPUT);
  Spark.function("cook",cook);
  Spark.function("stopcook",stopcook);
  Spark.function("opendoor",opendoor);
}

// This routine gets called repeatedly, like once every 5-15 milliseconds.
// Spark firmware interleaves background CPU activity associated with WiFi + Cloud activity with your code. 
// Make sure none of your code delays or blocks for too long (like more than 5 seconds), or weird things can happen.
void loop() {
    if (flag)
    {
      elapsedMillis timeElapsed;
      int interval = flagvalue;
      while (timeElapsed < interval && flag) 
      {
          digitalWrite(led, HIGH);   // Turn ON the LED pins
          digitalWrite(led2, HIGH);
         
          delay(10);               // Wait for 1000mS = 1 second
      }
      
      digitalWrite(led, LOW);    // Turn OFF the LED pins
      digitalWrite(led2, LOW); 
      openDoor();
      outputmusic();
      flag = 0;
      flagvalue = 0;
    }
    else
    {
        if(open_door_flag)
        {
        openDoor();
        open_door_flag = 0;
        }
    }
}

