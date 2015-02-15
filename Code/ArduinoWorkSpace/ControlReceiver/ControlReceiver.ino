const int RearMotorForwardPin = 8;
const int RearMotorReversePin = 9;
const int FrontMotorLeftPin = 10;
const int FrontMotorRightPin = 11;
int commandFromController = 0;

void setup() { 
  Serial.begin(9600);
  
  pinMode(RearMotorForwardPin, OUTPUT);   
  pinMode(RearMotorReversePin, OUTPUT);   
  pinMode(FrontMotorLeftPin, OUTPUT);   
  pinMode(FrontMotorRightPin, OUTPUT);     
}


void loop() {
  if(Serial.available() > 0){
    //Convert ASCII code to corresponding integer value by subtracting with the ASCII code for zero
    commandFromController = Serial.read() - '0';
    //Serial.print("Control Receiver Arduino received: ");
    //Serial.println(commandFromController, DEC); 
    
    if(commandFromController == 0){
      GoForward();
      StopAll();
    }
    else if(commandFromController == 2){
      GoRight();
      StopAll();
    }
    else if(commandFromController == 3){
      GoLeft();
      StopAll();
    }
    else{
      //Do Nothing
      StopAll();
    }
  }
  else{
   //StopAll();
  } 
}

void GoForward(){
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, HIGH);
  delay(200);
  digitalWrite(RearMotorForwardPin, LOW);
}

void GoBackward(){
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(RearMotorReversePin, HIGH);
  delay(200);
  digitalWrite(RearMotorReversePin, LOW);
}

void GoLeft(){
  digitalWrite(FrontMotorRightPin, LOW);
  digitalWrite(FrontMotorLeftPin, HIGH);
  delay(200);
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, HIGH);
  delay(200);
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(FrontMotorLeftPin, LOW);
}

void GoRight(){
  digitalWrite(FrontMotorLeftPin, LOW);
  digitalWrite(FrontMotorRightPin, HIGH);
  delay(200);
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, HIGH);
  delay(200);
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(FrontMotorRightPin, LOW);
}

void StopAll(){
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(FrontMotorLeftPin, LOW);
  digitalWrite(FrontMotorRightPin, LOW);
}
