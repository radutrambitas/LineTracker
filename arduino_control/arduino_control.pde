#include <MeetAndroid.h>

// declare MeetAndroid so that you can call functions with it
MeetAndroid meetAndroid;

const int motor1enablePin = 9;    // H-bridge enable pin
const int motor1PIN1A = 3;    // H-bridge leg 1 (pin 2, 1A)
const int motor1PIN2A = 4;    // H-bridge leg 2 (pin 7, 2A)
const int motor2enablePin = 10;    // H-bridge enable pin
const int motor2PIN3A = 5;    // H-bridge leg 3 (pin 10, 3A)
const int motor2PIN4A = 6;    // H-bridge leg 4 (pin 15, 4A)
const int switchPin = 12; // On/off Switch
const int ledPin = 13;      // LED 

//Motor Speed/Direction vars
int direction = 0; // 0=Forward, 1=Reverse
int motor1speed = 0; // Right motor
int motor2speed = 0; // Left motor
unsigned long lastcmd;
int cmdTimeout = 200; // timeout

int switchState = 0;
int switchReading;
int switchPrev = LOW;
long switchTime = 0;
long switchDebounce = 200;

void setup() {
  // use the baud rate your bluetooth module is configured to 
  // not all baud rates are working well, i.e. ATMEGA168 works best with 57600
  Serial.begin(57600); 
  
  // register callback functions, which will be called when an associated event occurs.
  meetAndroid.registerFunction(set_motor_speed, 's');
  
  // set all output pins
  pinMode(motor1PIN1A, OUTPUT); 
  pinMode(motor1PIN2A, OUTPUT); 
  pinMode(motor1enablePin, OUTPUT);
  pinMode(motor2PIN3A, OUTPUT); 
  pinMode(motor2PIN4A, OUTPUT); 
  pinMode(motor2enablePin, OUTPUT);	
  pinMode(ledPin, OUTPUT);
  pinMode(switchPin, INPUT);
}

void motor_drive(int dir, int rspeed, int lspeed)
{ 
  switchReading = digitalRead(switchPin);

  // if the input just went from LOW and HIGH and we've waited long enough
  // to ignore any noise on the circuit, toggle the output pin and remember
  // the time
  if (switchReading == HIGH && switchPrev == LOW && millis() - switchTime > switchDebounce) {
    if (switchState == HIGH) {
      switchState = LOW;
      lastcmd = 0; //immediate stop
    }
    else
      switchState = HIGH;

    switchTime = millis();
    
    //Switch state changed... update Android
    meetAndroid.send(switchState);
  }
  digitalWrite(ledPin,switchState);
  switchPrev = switchReading;
  
  // Reverse
  if (dir == 1) 
  {
    digitalWrite(motor1PIN1A, HIGH);
    digitalWrite(motor1PIN2A, LOW);
    digitalWrite(motor2PIN3A, HIGH);
    digitalWrite(motor2PIN4A, LOW); 
  }
  //Forward  
  else
  {
    digitalWrite(motor1PIN1A, LOW);
    digitalWrite(motor1PIN2A, HIGH);
    digitalWrite(motor2PIN3A, LOW);
    digitalWrite(motor2PIN4A, HIGH);
  }
  
  analogWrite(motor1enablePin, rspeed);
  analogWrite(motor2enablePin, lspeed);
}

void set_motor_speed(byte flag, byte numOfValues)
{
  if (!switchState) return;
  
  int data[numOfValues];
  meetAndroid.getIntValues(data);
  
  direction = data[0];
  motor1speed = data[1];
  motor2speed = data[2];  
  
  lastcmd = millis();
}

void loop() {
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
  motor_drive(direction,motor1speed,motor2speed);

  // if more than cmdTimeout milliseconds have passed with no input, stop motors
  if (millis()-lastcmd > cmdTimeout) {
    motor1speed=0;
    motor2speed=0;  
  }
}
