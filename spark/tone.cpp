#define DURATION	1000000
#define NOTE_BF4	1073
#define NOTE_AF4	1205
#define NOTE_F4		1433
#define NOTE_EF5	804
#define NOTE_D5		852
#define PAUSE		1000000

int funkyTown[11] = {NOTE_BF4, NOTE_BF4, NOTE_AF4, NOTE_BF4, 
					 PAUSE, NOTE_F4, PAUSE, NOTE_F4,
					 NOTE_BF4, NOTE_EF5, NOTE_D5, NOTE_BF4};

int16_t audioPin = D6;
int16_t x,y = 0;

// Add this into the void setup()
pinMode(audioPin, OUTPUT);


// Add this to the end of cook or whatever
// Loop over 12x for each of the notes
// for (i = 0; i < 12; i++) 
// takeMeTo(audioPin, funkyTown[i])
void takeMeTo(int pin, int note){
	for(x = 0; x < (DURATION/note); x++) {
	    PIN_MAP[pin].gpio_peripheral->BSRR = PIN_MAP[pin].gpio_pin; // HIGH
	    delayMicroseconds(note);
	    PIN_MAP[pin].gpio_peripheral->BRR = PIN_MAP[pin].gpio_pin;  // LOW
	    delayMicroseconds(note);
	  }
	  y++;
	  if(y >= MAX_NOTES) y = 0;
	  delay(250);
}

