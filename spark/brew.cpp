/*
 * nextWAVE
 * A project for HackIllinois 2014
 *
 * Dario Aranguiz :: aranguizdario@gmail.com
 * Kashev Dalmia  :: kashev.dalmia@gmail.com
 * Brady Salz     :: brady.salz@gmail.com
 * Ahmed Suhyl    :: sulaimn2@illinois.edu
 * 
 * brew.cpp
 */

/*
 * Device ID :
 *   48ff70065067555028111587
 *
 * Access Token :
 *   4348526a1c0932c678d6e971ce456b9d2ea4a1f5
 */

#define PIN_MICROWAVE D0
#define STATUS_LED    D7 // This one is the built-in tiny one to the right of the USB jack

/*
 * FUNCTIONSs
 */
int
brew (String args)
{
  int value_time = atoi(args.c_str());
  digitalWrite(PIN_MICROWAVE, HIGH);   // Turn ON the LED pins
  digitalWrite(STATUS_LED, HIGH);
  delay(value_time);               // Wait for 1000mS = 1 second
  digitalWrite(PIN_MICROWAVE, LOW);    // Turn OFF the LED pins
  digitalWrite(STATUS_LED, LOW); 
  delay(value_time);               // Wait for 1 second in off mode
  return value_time;

}


/*
 * SPARK REQUIRED
 */

void
setup (void)
{
  pinMode(PIN_MICROWAVE, OUTPUT);
  pinMode(STATUS_LED,    OUTPUT);
  Spark.function("brew", brew);
}

// This routine gets called repeatedly, once every 5-15 milliseconds.
// Spark firmware interleaves background CPU activity associated with WiFi + Cloud activity with your code. 
// Make sure none of your code delays or blocks for too long (like more than 5 seconds), or weird things can happen.
void
loop (void)
{
  // digitalWrite(led, HIGH);   // Turn ON the LED pins
  // digitalWrite(led2, HIGH);
  // delay(1000);               // Wait for 1000mS = 1 second
  // digitalWrite(led, LOW);    // Turn OFF the LED pins
  // digitalWrite(led2, LOW); 
  // delay(1000);               // Wait for 1 second in off mode
}
