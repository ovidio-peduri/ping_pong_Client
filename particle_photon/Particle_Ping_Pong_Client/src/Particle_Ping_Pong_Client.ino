#include "clickButton.h"

const static int SINGLE_CLICK = 1;
const static int DOUBLE_CLICK = 2;
const static int TRIPLE_CLICK = 3;
const static int SINGLE_LONG_CLICK = -1;
const static int DOUBLE_LONG_CLICK = -2;
const static int TRIPLE_LONG_CLICK = -3;
const static int NO_CLICK = 0;

const static int NUM_SCORE_BUTTONS = 2;
const static int NUM_MULTIFUNCTION_BUTTONS = 2;

int A_Score = D0;
int B_Score = D1;

int A_Multifunction_Btn = D2;
int B_Multifunction_Btn = D3;

int led = D7;
ClickButton scoreButtons [NUM_SCORE_BUTTONS] = {
  {A_Score, HIGH},
  {B_Score, HIGH},
};

ClickButton multifunctionButtons [NUM_MULTIFUNCTION_BUTTONS] = {
  {A_Multifunction_Btn, HIGH},
  {B_Multifunction_Btn, HIGH}
};

//SETUP
void setup() {
  //Use the external antenna
  WiFi.selectAntenna(ANT_EXTERNAL);

  pinMode(led, OUTPUT);
  //Setup all the buttons for input
  pinMode(A_Score, INPUT_PULLDOWN);
  pinMode(B_Score, INPUT_PULLDOWN);
  pinMode(A_Multifunction_Btn, INPUT_PULLDOWN);
  pinMode(B_Multifunction_Btn, INPUT_PULLDOWN);

  //Setup click buttons
  for (int i = 0; i< NUM_SCORE_BUTTONS; i++) {
    scoreButtons[i].debounceTime = 50;
    scoreButtons[i].multiclickTime = 250;
    scoreButtons[i].longClickTime = 1000;
  }
  for (int i = 0; i< NUM_MULTIFUNCTION_BUTTONS; i++) {
    multifunctionButtons[i].debounceTime = 50;
    multifunctionButtons[i].multiclickTime = 250;
    multifunctionButtons[i].longClickTime = 1000;
  }
}

//MAIN PROGRAM
void loop() {
  updateScoreButtons();
  updateMultifunctionButtons();
  delay(10);
  handleScoreButtons();
  handleMultifunctionButtons();
}

void updateScoreButtons() {
  for (int i = 0; i< NUM_SCORE_BUTTONS; i++) {
    scoreButtons[i].Update();
  }
}

void updateMultifunctionButtons() {
  for (int i = 0; i< NUM_MULTIFUNCTION_BUTTONS; i++) {
    multifunctionButtons[i].Update();
  }
}

void handleScoreButtons()
{
  for (int i = 0; i< NUM_SCORE_BUTTONS; i++) {
    if (scoreButtons[i].clicks == SINGLE_CLICK) {
      //Publish event where it will be handled by
      //a particle webhook
      if (i > 0) {
        Particle.publish("B_INCREMENT");
      } else {
        Particle.publish("A_INCREMENT");
      }
      //Only 1 team should increment at a time
      //Ignore all other score button presses
      return;
    } else if (scoreButtons[i].clicks == SINGLE_LONG_CLICK) {
      if (i > 0) {
        Particle.publish("B_DECREMENT");
      } else {
        Particle.publish("A_DECREMENT");
      }
    }
  }
}

void handleMultifunctionButtons()
{
  //For now these buttons just turn an led on or off for testing
  for (int i = 0; i< NUM_MULTIFUNCTION_BUTTONS; i++) {
    if (multifunctionButtons[i].clicks == SINGLE_CLICK) {
      if (i > 0) {
        Particle.publish("B_TAUNT");
      } else {
        Particle.publish("A_TAUNT");
      }
    }
  }
}
