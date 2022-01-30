#include <OneWire.h>
#include <DallasTemperature.h>
#define LED_RED 12
#define LED_YELLOW 11
#define ONE_WIRE_BUS 2

OneWire oneWire(ONE_WIRE_BUS);  
DallasTemperature sensors(&oneWire);

void setup(void)
{
  sensors.begin();
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);

//  turnOnLED(LED_RED);
//  turnOnLED(LED_YELLOW);


  Serial.begin(9600);
}

void turnOnLED(int LED) {
  digitalWrite(LED, HIGH);
  delay(1000);
  digitalWrite(LED, LOW);
}

void loop(void)
{ 
  if (Serial.available() > 0) {
    String tmp = Serial.readString();
//    Serial.println("Input: " + tmp);
//    turnOnLED(LED_RED);
    if (tmp.indexOf("Request Data") >= 0) {
//      turnOnLED(LED_YELLOW);
          //reads and print light sensor's values
      int analogValue = analogRead(A0);
      sensors.requestTemperatures(); 

      String res = "Result/" + String(analogValue) + "/" + String(sensors.getTempCByIndex(0));
      Serial.println(res);
    }
    tmp = "";
  }
  delay(100);
}
