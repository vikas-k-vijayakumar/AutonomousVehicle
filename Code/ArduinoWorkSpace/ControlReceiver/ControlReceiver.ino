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
      delay(1000);
      Stop();
    }
    else if(commandFromController == 2){
      GoRight();
      delay(1000);
      Stop();
    }
    else if(commandFromController == 3){
      GoLeft();
      delay(1000);
      Stop();
    }
    else{
      //Do Nothing
      Stop();
    }
  }
  else{
   Stop();
  } 
}

void GoForward(){
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, HIGH);
}

void GoBackward(){
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(RearMotorReversePin, HIGH);
}

void GoLeft(){
  Stop();
  digitalWrite(FrontMotorRightPin, LOW);
  digitalWrite(FrontMotorLeftPin, HIGH);
  GoForward();
}

void GoRight(){
  Stop();
  digitalWrite(FrontMotorLeftPin, LOW);
  digitalWrite(FrontMotorRightPin, HIGH);
  GoForward();
}

void Stop(){
  digitalWrite(RearMotorReversePin, LOW);
  digitalWrite(RearMotorForwardPin, LOW);
  digitalWrite(FrontMotorLeftPin, LOW);
  digitalWrite(FrontMotorRightPin, LOW);
}
