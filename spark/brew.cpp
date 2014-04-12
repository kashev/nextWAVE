
blink-an-led.ino  

1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
//Device ID
//48ff70065067555028111587
//Access Token
//4348526a1c0932c678d6e971ce456b9d2ea4a1f5
// Define the pins we're going to call pinMode on
int led = D0;  // You'll need to wire an LED to this one to see it blink.
int led2 = D7; // This one is the built-in tiny one to the right of the USB jack
volatile int flag = 0;
int flagvalue = 0;
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
Ready.