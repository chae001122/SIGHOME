#include <ESP8266WiFi.h>
#include <PubSubClient.h>     // for mqtt communication
#include <SoftwareSerial.h>
#include <string.h>

// wifi information (must change it into your wifi env)
const char* ssid = "SeyeonLand";
const char* password = "01047533589";
const char* mqtt_server = "192.168.0.6";
char ard_msg;

WiFiClient espClient;
PubSubClient client(espClient);
SoftwareSerial s(D6, D5);

// set wifi environment
void setup_wifi() {

  s.begin(9600);
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status( ) != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());
  
  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP( ));
}

// when rasberry pi publish to nodemcu 
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  Serial.println();

  if (!strcmp(topic, "/pause")) {
    Serial.println("pause");
    //wait();
  } else if(!strcmp(topic, "/resume")){
    //Serial.println("1try again int 5 seconds");
    delay(5000);
    //Serial.println("1try again int seconds");
    //digitalWrite(BUILTIN_LED, HIGH);  // Turn the LED off by making the voltage HIGH
  }
}


void reconnect() {
  while(!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientID = "ESP8266Client";
    //clientID += String(random(0xffff), HEX);
    if (client.connect(clientID.c_str())) {
      Serial.println("connected");
      //client.subscribe("/pause");
      client.subscribe("/resume");
      //client.subscribe("/bell");
      //client.subscribe("/garage");
      //client.publish("/inmode/sensor/window", "human detected");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println("try again int 5 seconds");
      delay(5000);
    }
  } 
}

void wait() {
  while(1) {
    delay(10);
  }
}


void setup() {
  pinMode(BUILTIN_LED, OUTPUT);
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void loop() {
  if(s.available()) {
    ard_msg = s.read();
    Serial.println(ard_msg);
    if(ard_msg == 'h') {
      Serial.println("human"); 
      if (!client.connected()) {
        reconnect();
      }
      client.publish("/sensor/enter", "human detected");
    } else if(ard_msg == 'b') {
      Serial.println("bell");
      if (!client.connected()) {
        reconnect();
      }
      client.publish("/sensor/bell", "bell is pushed");
    } else if(ard_msg == 'f') {
      Serial.println("fire");
      if (!client.connected()) {
        reconnect();
      }
      client.publish("/sensor/fire", "fire is detected");
    } else if(ard_msg == 'g') {
      Serial.println("car");
      if (!client.connected()) {
        reconnect();
      }
      client.publish("/sensor/garage", "car is moved");
    } else if(ard_msg == 'w') {
      Serial.println("car");
      if (!client.connected()) {
        reconnect();
      }
      client.publish("/sensor/window", "window detected");
    }
  }
  client.loop();
}
 
