#include <QTRSensors.h>
#include <Encoder.h>
#include "Seguidor.h"

#define Kp 0.5
#define Ki 0
#define Kd 4

//#define TELEMETRIA_ON

#ifndef TELEMETRIA_ON
  #define encoderA1_pin 2
  #define encoderB1_pin 1
  #define encoderA2_pin 3
  #define encoderB2_pin 0
#else
  #define encoderA1_pin 2
  #define encoderB1_pin 9
  #define encoderA2_pin 3
  #define encoderB2_pin 14
#endif

#define nSensors 8
#define muestras_sensor 4

QTRSensorsAnalog qtra((unsigned char[]){9,5,4,11,3,1,2,0},nSensors,muestras_sensor, QTR_NO_EMITTER_PIN);
unsigned int valuesS[nSensors], line_pos;

long pE1, pE2;
Encoder encoder_M1(encoderA1_pin, encoderB1_pin), encoder_M2(encoderA2_pin,encoderB2_pin);

Seguidor seguidor(Kp,Ki,Kd);//cambiar 0's por las constantes
void setup(){
  pinMode(encoderA1_pin,INPUT);
  pinMode(encoderA2_pin,INPUT);
  pinMode(encoderB1_pin,INPUT);
  pinMode(encoderB2_pin,INPUT);

  /*Serial.println("Inicio C");
  for (int i = 0; i < 400; i++)  // make the calibration take about 10 seconds
  {
    qtra.calibrate();       // reads all sensors 10 times at 2.5 ms per six sensors (i.e. ~25 ms per call)
  }
  Serial.println("Fin C");*/
}

void loop(){
  if(seguidor.isRobot_active()){
    line_pos = qtra.readLine(valuesS);
    seguidor.set_SensorsValues_LinePosition(valuesS,line_pos);

    /*lee encoders y pone sus valores*/
    pE1 = encoder_M1.read();
    pE2 = encoder_M2.read();

    seguidor.set_Positions_Encoders(pE1,pE2);

  }

  seguidor.runing_Seguidor();
}