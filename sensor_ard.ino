#include <SoftwareSerial.h>

int TILT_pin = 7;   // Tilt sensor
int PIR_pin = 12;   // PIR sensor
int SW_pin = 13;    // Switch module
int trig_pin = 9;   // ultrasonic wave sensor
int echo_pin = 8;   // ,,
int piezo_pin = 10; // piezo buzzer
int FLAME_pin = 11; // Flame sensor

SoftwareSerial s(2, 3); // for serial communication with nodemcu

void setup() {
  pinMode(TILT_pin, INPUT);
  pinMode(PIR_pin, INPUT);
  pinMode(SW_pin, INPUT);
  pinMode(trig_pin,OUTPUT);
  pinMode(echo_pin, INPUT);
  pinMode(FLAME_pin, INPUT);
  Serial.begin(9600);
  s.begin(9600);
}

void loop() {
  int ifwindow = digitalRead(TILT_pin); // Tilt sensor val
  int ifHuman = digitalRead(PIR_pin);   // PIR sensor val
  int ifPushed = digitalRead(SW_pin);   // Switch val
  int ifFire = digitalRead(FLAME_pin);  // Flame sensor val
  
  // calculate ultrasonic wave sensor val
  digitalWrite(trig_pin, LOW);
  digitalWrite(echo_pin, LOW);
  delayMicroseconds(2);
  digitalWrite(trig_pin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig_pin, LOW);
  unsigned long duration = pulseIn(echo_pin, HIGH); 
  float distance = ((float)(340 * duration) / 10000) / 2;
 
  // human detected
  if(ifHuman == HIGH){
    Serial.println("detected");
    s.write('h');
    delay(1000);                            
  }
  // switch pushed 
  if(ifPushed == HIGH) {
    Serial.println("pushed");
    s.write('b');
    tone(piezo_pin, 400);
    delay(1000);
    noTone(piezo_pin);  
  } 
  // window detected
  if(ifwindow == LOW) {
    Serial.println("window");
    s.write('w');
    delay(100);
  }
  // flame detected
  if(ifFire == 1){
    Serial.println("fired");
    s.write('f');
    delay(1000);
  }
  // car is gone
  if(distance >= 13.0) {
    Serial.println("nothing");
    delay(1000);
  }
  else if(distance >= 8.0) {
    Serial.println("car is moved");
    s.write('g');
    delay(1000);
  } 
  else if(distance<8.0){
    //nothing
    delay(1000);
  }
  
}
