import processing.sound.*;
import java.util.*;

//-------------------------------------------------------------------------------
//Setup game
int framerate = 60;

void setup() {
  fullScreen();
  frameRate(framerate);
  setupGameStates();
  setupBackground();
  setupSound();
  setupEntities();
  resetData();
  println("Game loaded successfully");
}

void setupGameStates() {
  gameMenu = true;
  gameSettings = false;
  levelPassed = false;
  gameSensitivity = false;
  gameSoundInput = false;
  getFreqRange = false;
}

void setupSound() {
  soundOn();
  importSounds();
  println("Sound on and imported sounds successfully");
}

void setupBackground() {
  loadBackground();
  println("Loaded background successfully");
}

void setupEntities() {
  importLevelEntities();
  createFish();
  println("Imported entities successfully");
}

//-------------------------------------------------------------------------------
//Game logic
Fish fish;
int passed, attempts;
int enemiesPerLevel = 5;
int sensitivity;

void draw() {
  drawBackground();
  if (gameMenu) gameMenu();
  else if (gameSettings) gameSettings();
  else if (gameSensitivity) gameSensitivityMenu();
  else if (gameSoundInput) gameSoundInputMenu();  
  else {
    displayLevel();
    play();
  }
  procObstacles(fish);
}

void play() {
  if (winning()) win();
  else if (passingLevel()) levelPassed();
  else if (playingGame()) fish.move(new PVector(0, getAcceleration()), obstacles);
  else if (losing()) lose();
}

//gets the vertical acceleration of the fishy
float getAcceleration() {
  if (soundInputType == 0) return getAccelerationAmp();
  return getAccelerationFreq();
}

//----------------------------------------------------------------------------------
//Menu

void gameMenu() {
  display4CenterText("soundFish", "Press ENTER to PLAY", "Press S for Settings", "Press ESC to QUIT");
  if (keyPressed && key == ENTER) {
    gameMenu = false;
    resetGame();
  }
  else if (keyPressed && key == 's') {
    gameMenu = false;
    gameSettings = true;
    gameSettings();
  }
  else if (keyPressed && key == ESC) {
    exitGame();
  }
}

//type of sound acc (freq or amp, default = amp), sensitivity (1-100, default = 50)
void gameSettings() {
  display4CenterText("Settings", "Press F for SOUND CONTROL", "Press D for SENSITIVITY", "Press BACKSPACE to MAIN MENU");
  if (keyPressed && key == 'f') {
    gameSettings = false;
    gameSoundInput = true;
  }
  else if (keyPressed && key == 'd') {
    gameSettings = false;
    gameSensitivity = true;
  }
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
    gameSettings = false;
  }
}

void gameSensitivityMenu() {
  display4CenterText("Sensitivity", "Current sensitivity (1-100): " + sensitivity, "Use LEFT/RIGHT arrow keys", "Press S to SETTINGS");
  if (keyPressed && keyCode == LEFT) {
    if (sensitivity > 1) sensitivity--;
  }
  else if (keyPressed && keyCode == RIGHT) {
    if (sensitivity < 100) sensitivity++;
  }
  else if (keyPressed && key == 's') {
    gameSensitivity = false;
    gameSettings = true;
  }  
}

//freq or amp (default is amp)
void gameSoundInputMenu() {
  String dispType = "AMPLITUDE  Frequency";
  if (soundInputType == 1) dispType = "Amplitude  FREQUENCY";
  display4CenterText("Sound Input", dispType, "Use LEFT/RIGHT arrows to SWITCH", "Press S to SETTINGS");
  if (keyPressed && keyCode == LEFT) {
    soundInputType = 0;
  }
  else if (keyPressed && keyCode == RIGHT) {
    soundInputType = 1;
  }
  else if (keyPressed && key == 's') {
    if (soundInputType == 1) getFreqRange = true;
    else gameSettings = true;
    gameSoundInput = false;
  }  
}

//frequency calibration
void getFreqRange() {

}

//----------------------------------------------------------------------------------
//Displaying texts/backgrounds
PImage bground;

void loadBackground() {
  bground = loadImage("background.png");
  bground.resize(width, height);
}

void displayLevel() {
  textAlign(RIGHT, TOP);
  textSize(40);
  fill(255, 0, 0);
  text("Level: " + level, width, 0);
  text("Obstacles passed: " + passed, width, 40);
  text("Attempts: " + attempts, width, 80);
}

void drawBackground() {
  imageMode(CENTER);
  image(bground, width/2, height/2);
}

void display3CenterText(String top, String middle, String bottom) {
  textAlign(CENTER, CENTER);
  textSize(200);
  fill(255, 0, 0);
  text(top, width/2, height/2-100);
  textSize(100);
  text(middle, width/2, height/2 + 50);
  text(bottom, width/2, height/2 + 150);
}

void display4CenterText(String top, String middle1, String middle2, String bottom) {
  display3CenterText(top, middle1, middle2);
  text(bottom, width/2, height/2 + 250);
}

//----------------------------------------------------------------------------------
//End game methods

void win() {
  win = true;
  playSound(Fanfare);
  display3CenterText("YOU WIN!", "Press ENTER to PLAY AGAIN", "Press BACKSPACE to QUIT");
  if (keyPressed && key == ENTER) {
    levelPassed = false;
    attempts = 0;
    resetData();
  }
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
    levelPassed = false;
  }
}

void lose() {
  levelPassed = false;
  display3CenterText("You lose!", "Press ENTER to PLAY AGAIN", "Press BACKSPACE to QUIT");
  playSound(WahWahWah);
  if (keyPressed && key == ENTER) resetData();
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
  }
}

void exitGame() {
  soundOff();
  exit();
}

//-----------------------------------------------------------------------------------
//Game States
boolean win, gameMenu, gameSettings, gameSensitivity, gameSoundInput, getFreqRange;

boolean winning() {
  return (win || (passingLevel() && (level == level_entities.size())));
}

boolean passingLevel() {
  return (levelPassed || (!fish.dead && passed == level*enemiesPerLevel));
}

boolean playingGame() {
  return (!fish.dead && passed < level*enemiesPerLevel);
}

boolean losing() {
  return (fish.dead && passed < level*enemiesPerLevel);
}

//-----------------------------------------------------------------------------------
//Levels
int level;
boolean levelPassed;

void levelPassed() {
  levelPassed = true;
  display3CenterText("Level passed!", "Press ENTER to CONTINUE", "Press BACKSPACE to QUIT");
  playSound(Yay);
  if (keyPressed && key == ENTER) resetData();
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
    levelPassed = false;
  }
}

void toLevel1() {
  level = 1;
  attempts += 1;
}

void nextLevel() {
  level += 1;
}

//----------------------------------------------------------------------------------
//Resets

void resetLevel() {
  resetFish();
  resetObstacles();
}

//reset sound and data
void resetGame() {
  attempts = 0;
  resetData();
}

void resetData() {
  if (levelPassed) nextLevel();
  else toLevel1();
  resetLevel();
  levelPassed = false;
  win = false;
  soundPlayed = false;
}

//resets the fish's position to the vertical middle of the screen
//resets the fish's death counter to alive
void resetFish() {
  fish.reset();
}

//resets the obstacles to 0 and score to 0
//gets the obstacle velocity for the current next level
void resetObstacles() {
  obstacles = new ArrayList<Obstacle>();
  objectVel = getObsVel();
  passed = 0;
}

//-------------------------------------------------------------------------------
//Sound
AudioIn in;
FFT fft;
Amplitude amp;
SoundFile WahWahWah, Fanfare, Yay;
boolean soundPlayed;
//0 is default (amp) and 1 is freq
int soundInputType, bands, lowerFreq, upperFreq;

void importSounds(){
  try {
    WahWahWah = new SoundFile(this, "WahWahWah.mp3");
    Fanfare = new SoundFile(this, "Fanfare.mp3");
    Yay = new SoundFile(this, "Yay.mp3");
  }
  catch (Exception e){
    print("Could not find sound file");
  }
}

void playSound(SoundFile toPlay) {
  if (!toPlay.isPlaying() && !soundPlayed) {
    soundPlayed = true;
    toPlay.play();
  }
}

void soundOn() {
  in = new AudioIn(this, 0);
  in.start();
  soundOnAmp();
  soundOnFFT();
  soundPlayed = false;
  soundInputType = 0;
  sensitivity = 50;
}

//turn FFT on 
void soundOnFFT() {
  try {
    bands = 512;
    fft = new FFT(this, bands);
    fft.input(in);
    println("FFT turned on successfully");
  }
  catch (Exception e) {
    println("Error: FFT NOT turned on successfully");
    e.printStackTrace();
  }
}

//turn amplitude analysis on
void soundOnAmp() {
  try {
    amp = new Amplitude(this);
    amp.input(in);
    println("Amplitude analysis turned on successfully");
  }
  catch (Exception e) {
    println("Error: Amplitude analysis NOT turned on successfully");
    e.printStackTrace();
  }
}

void soundOff() {
  in.stop();
}

//returns an acceleration based off of the loudness of noise produced
float getAccelerationAmp() {
  float db = amp.analyze();
  if (db < 0.01) return 0.75;
  float acc = -(db*sensitivity/20);
  return limitAcc(acc);
}

//returns and accerlation based off the pitch of noise produced
float getAccelerationFreq() {
  fft.analyze();
  /*float lower = 0, upper = 0;
  for (int i = 0 ; i < bands/2 ; i++) lower += fft.spectrum[i];
  for (int i = bands/2 ; i < bands ; i++) upper += fft.spectrum[i];
  float acc = (lower-upper)*1.0*sensitivity/(lower+upper);*/
  float avg = 0;
  for (int i = 0 ; i < bands ; i++) avg += fft.spectrum[i] * i;
  float acc = map(avg, lowerFreq, upperFreq, -3, 3) * sensitivity;
  return limitAcc(acc);
}

//limits acc to max 3
float limitAcc(float acc) {
  if (acc < -3) return -3;
  else if (acc > 3) return 3;
  return acc;
}

//--------------------------------------------------------------------------------
//Sprites and Images
BufferedReader read_data;
HashMap<String, PImage> sprites;
ArrayList<String[]> level_entities;

void importLevelEntities() {
  level_entities = new ArrayList<String[]>();
  sprites = new HashMap<String, PImage>();
  loadUnknownImg();
  try {
    read_data = createReader("level_entities.txt");
    for (String line = read_data.readLine(); line != null; line = read_data.readLine())
      processLine(line);
    processName("fish");
  }
  catch (Exception e) {
    e.printStackTrace();
  }
}

//loads the "unknown" image for missing sprites
void loadUnknownImg() {
  processName("unknown");
}

//resize an image without altering the original copy
//to preserve pixel information for compression algorithms
PImage resizeImage(PImage image, int imgW, int imgH) {
  PImage returnImage = image.copy();
  returnImage.resize(imgW, imgH);
  return returnImage;
}

//creates the fish
void createFish() {
  PImage fish_img = sprites.get("fish");
  int imgH = int(height*1.0/10);
  int imgW = getPropWidth(fish_img, imgH);
  fish_img = resizeImage(fish_img, imgW, imgH);
  fish = new Fish(new PVector(width/10, height/2), fish_img);
}

//-----------------------------------------------------------------------------------
//Sprites: text to img objects

//takes in an line of text and converts it into a set of name-sprite bindings
void processLine(String line) {
  String[] splitted = split(line, ' ');
  level_entities.add(splitted);
  processNames(splitted);
}

//takes in a list of names and processes them into name-sprite bindings
void processNames(String[] splitted) {
  for (String name : splitted) processName(name);
}

//takes in a name and creates that name-sprite binding
void processName(String name) {
  PImage img = importImage(name);
  storeSprite(name, img);
}

//takes in a name and sprite and stores it in the hashmap sprites
//if the sprite name is not already in the keys of the hashmap
void storeSprite(String name, PImage sprite) {
  if (!sprites.containsKey(name)) sprites.put(name, sprite);
}

//loads and returns an image corresponding to the input name
//if the image is not found, return the "unknown" image
PImage importImage(String name) {
  PImage img = loadImage(name + ".png");
  if (img != null) return img;
  print("Image not found: " + name);
  if (!name.equals("unknown")) return sprites.get("unknown");
  return null;
}

//-----------------------------------------------------------------------------------
//Obstacles
float objectVel;
ArrayList<Obstacle> obstacles;

//add obstacles every depending on level/total_levels
//there is a gap between starting the level and the first obstacle
//but who cares lmao
void procObstacles(Fish fish) {
  if (frameCount%(getFramesPerObs()) == 0) addObstacle();
  moveObstacles(fish);
}

//add a random obstacle from the correct level in level_entities
void addObstacle() {
  String name = getRandomName();
  PImage obs_image = resizeObstacle(name);
  obstacles.add(nameToObj(name, obs_image));
}

void moveObstacles(Fish fish) {
  for (int i = obstacles.size()-1; i >= 0; i--) {
    obstacles.get(i).move(objectVel, fish);
    if (!fish.dead && !win && !levelPassed && obstacles.get(i).passed) {
      passed++;
      obstacles.remove(i);
    }
  }
}

//-----------------------------------------------------------------------------------
//Obstacle maths

int getFramesPerObs() {
  return int(map(level, 1, level_entities.size(), framerate*2, int(framerate/2)));
}

//returns a random pixel height that is scaled towards the edges of the screen
//favors players who have the control to keep their character in the middle of the screen
//favors skill
int getPlaceHeight() {
  int parity = (random(0, 1) > 0.5) ? -1 : 1;
  float place_height = (random(0, height/2)*parity*1.1)+height/2;
  if (place_height < getObsH()/2 || height-place_height < getObsH()/2) place_height = getObsH()/2;
  return int(place_height);
}

//returns the current obstacle velocity for that level
//first level obstacles take 10 secs to travel across the screen
//final level obstacles take 3 secs to travel across the screen
float getObsVel() {
  float lowerBound = width*1.0/framerate/5;
  float upperBound = width*1.0/framerate/2;
  return -map(level, 1, level_entities.size(), lowerBound, upperBound);
}

//-----------------------------------------------------------------------------------
//Obstacle logic

//returns a level-appropriate random name from an array of entities
String getRandomName(){
  String[] entities = level_entities.get(level-1);
  return getRandom(entities);
}

//returns a random name from an array of entities
String getRandom(String[] entities) {
  return entities[int(random(0, entities.length))];
}

//takes in the datafile format name and a processed obstacle image
//returns an object of appropriate type
Obstacle nameToObj(String name, PImage obs_image) {
  if (name.equals("octopus")) return new Octopus(new PVector(width, getPlaceHeight()), obs_image);
  else if (name.equals("electric_eel")) return new ElectricEel(new PVector(width, getPlaceHeight()), obs_image);
  else return new Obstacle(new PVector(width, getPlaceHeight()), obs_image);
}

//--------------------------------------------------------------------------------------
//Obstacle sprite size

//takes in the name of an obstacle is sprites and returns a level-resized copy
PImage resizeObstacle(String name) {
  int imgH = getObsH();
  int imgW = getPropWidth(sprites.get(name), imgH);
  return resizeImage(sprites.get(name), imgW, imgH);
}

//returns the scaled width depending on an images original h/w ratio
int getPropWidth(PImage original, int imgH) {
  return int(original.width*1.0/original.height*imgH);
}

//return the current level's obstacle height
int getObsH() {
  int numLevels = level_entities.size();
  return int(map(level, 0, numLevels, height*1.0/15, height*1.0/10));
}

//-------------------------------------------------------------------------------------
