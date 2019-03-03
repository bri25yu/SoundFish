import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 
import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class soundFish extends PApplet {




//-------------------------------------------------------------------------------
//Setup game
int framerate = 60;

public void setup() {
  
  frameRate(framerate);
  setupGameStates();
  setupBackground();
  setupSound();
  setupEntities();
  resetData();
  println("Game loaded successfully");
}

public void setupGameStates() {
  gameMenu = true;
  gameSettings = false;
  levelPassed = false;
  gameSensitivity = false;
  gameSoundInput = false;
  getFreqRange = false;
}

public void setupSound() {
  soundOn();
  importSounds();
  println("Sound on and imported sounds successfully");
}

public void setupBackground() {
  loadBackground();
  println("Loaded background successfully");
}

public void setupEntities() {
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

public void draw() {
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

public void play() {
  if (winning()) win();
  else if (passingLevel()) levelPassed();
  else if (playingGame()) fish.move(new PVector(0, getAcceleration()), obstacles);
  else if (losing()) lose();
}

//gets the vertical acceleration of the fishy
public float getAcceleration() {
  if (soundInputType == 0) return getAccelerationAmp();
  return getAccelerationFreq();
}

//----------------------------------------------------------------------------------
//Menu

public void gameMenu() {
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
public void gameSettings() {
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

public void gameSensitivityMenu() {
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
public void gameSoundInputMenu() {
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
public void getFreqRange() {

}

//----------------------------------------------------------------------------------
//Displaying texts/backgrounds
PImage bground;

public void loadBackground() {
  bground = loadImage("background.png");
  bground.resize(width, height);
}

public void displayLevel() {
  textAlign(RIGHT, TOP);
  textSize(40);
  fill(255, 0, 0);
  text("Level: " + level, width, 0);
  text("Obstacles passed: " + passed, width, 40);
  text("Attempts: " + attempts, width, 80);
}

public void drawBackground() {
  imageMode(CENTER);
  image(bground, width/2, height/2);
}

public void display3CenterText(String top, String middle, String bottom) {
  textAlign(CENTER, CENTER);
  textSize(200);
  fill(255, 0, 0);
  text(top, width/2, height/2-100);
  textSize(100);
  text(middle, width/2, height/2 + 50);
  text(bottom, width/2, height/2 + 150);
}

public void display4CenterText(String top, String middle1, String middle2, String bottom) {
  display3CenterText(top, middle1, middle2);
  text(bottom, width/2, height/2 + 250);
}

//----------------------------------------------------------------------------------
//End game methods

public void win() {
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

public void lose() {
  levelPassed = false;
  display3CenterText("You lose!", "Press ENTER to PLAY AGAIN", "Press BACKSPACE to QUIT");
  playSound(WahWahWah);
  if (keyPressed && key == ENTER) resetData();
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
  }
}

public void exitGame() {
  soundOff();
  exit();
}

//-----------------------------------------------------------------------------------
//Game States
boolean win, gameMenu, gameSettings, gameSensitivity, gameSoundInput, getFreqRange;

public boolean winning() {
  return (win || (passingLevel() && (level == level_entities.size())));
}

public boolean passingLevel() {
  return (levelPassed || (!fish.dead && passed == level*enemiesPerLevel));
}

public boolean playingGame() {
  return (!fish.dead && passed < level*enemiesPerLevel);
}

public boolean losing() {
  return (fish.dead && passed < level*enemiesPerLevel);
}

//-----------------------------------------------------------------------------------
//Levels
int level;
boolean levelPassed;

public void levelPassed() {
  levelPassed = true;
  display3CenterText("Level passed!", "Press ENTER to CONTINUE", "Press BACKSPACE to QUIT");
  playSound(Yay);
  if (keyPressed && key == ENTER) resetData();
  else if (keyPressed && key == BACKSPACE) {
    gameMenu = true;
    levelPassed = false;
  }
}

public void toLevel1() {
  level = 1;
  attempts += 1;
}

public void nextLevel() {
  level += 1;
}

//----------------------------------------------------------------------------------
//Resets

public void resetLevel() {
  resetFish();
  resetObstacles();
}

//reset sound and data
public void resetGame() {
  attempts = 0;
  resetData();
}

public void resetData() {
  if (levelPassed) nextLevel();
  else toLevel1();
  resetLevel();
  levelPassed = false;
  win = false;
  soundPlayed = false;
}

//resets the fish's position to the vertical middle of the screen
//resets the fish's death counter to alive
public void resetFish() {
  fish.reset();
}

//resets the obstacles to 0 and score to 0
//gets the obstacle velocity for the current next level
public void resetObstacles() {
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

public void importSounds(){
  try {
    WahWahWah = new SoundFile(this, "WahWahWah.mp3");
    Fanfare = new SoundFile(this, "Fanfare.mp3");
    Yay = new SoundFile(this, "Yay.mp3");
  }
  catch (Exception e){
    print("Could not find sound file");
  }
}

public void playSound(SoundFile toPlay) {
  if (!toPlay.isPlaying() && !soundPlayed) {
    soundPlayed = true;
    toPlay.play();
  }
}

public void soundOn() {
  in = new AudioIn(this, 0);
  in.start();
  soundOnAmp();
  soundOnFFT();
  soundPlayed = false;
  soundInputType = 0;
  sensitivity = 50;
}

//turn FFT on 
public void soundOnFFT() {
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
public void soundOnAmp() {
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

public void soundOff() {
  in.stop();
}

//returns an acceleration based off of the loudness of noise produced
public float getAccelerationAmp() {
  float db = amp.analyze();
  if (db < 0.01f) return 0.75f;
  float acc = -(db*sensitivity/20);
  return limitAcc(acc);
}

//returns and accerlation based off the pitch of noise produced
public float getAccelerationFreq() {
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
public float limitAcc(float acc) {
  if (acc < -3) return -3;
  else if (acc > 3) return 3;
  return acc;
}

//--------------------------------------------------------------------------------
//Sprites and Images
BufferedReader read_data;
HashMap<String, PImage> sprites;
ArrayList<String[]> level_entities;

public void importLevelEntities() {
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
public void loadUnknownImg() {
  processName("unknown");
}

//resize an image without altering the original copy
//to preserve pixel information for compression algorithms
public PImage resizeImage(PImage image, int imgW, int imgH) {
  PImage returnImage = image.copy();
  returnImage.resize(imgW, imgH);
  return returnImage;
}

//creates the fish
public void createFish() {
  PImage fish_img = sprites.get("fish");
  int imgH = PApplet.parseInt(height*1.0f/10);
  int imgW = getPropWidth(fish_img, imgH);
  fish_img = resizeImage(fish_img, imgW, imgH);
  fish = new Fish(new PVector(width/10, height/2), fish_img);
}

//-----------------------------------------------------------------------------------
//Sprites: text to img objects

//takes in an line of text and converts it into a set of name-sprite bindings
public void processLine(String line) {
  String[] splitted = split(line, ' ');
  level_entities.add(splitted);
  processNames(splitted);
}

//takes in a list of names and processes them into name-sprite bindings
public void processNames(String[] splitted) {
  for (String name : splitted) processName(name);
}

//takes in a name and creates that name-sprite binding
public void processName(String name) {
  PImage img = importImage(name);
  storeSprite(name, img);
}

//takes in a name and sprite and stores it in the hashmap sprites
//if the sprite name is not already in the keys of the hashmap
public void storeSprite(String name, PImage sprite) {
  if (!sprites.containsKey(name)) sprites.put(name, sprite);
}

//loads and returns an image corresponding to the input name
//if the image is not found, return the "unknown" image
public PImage importImage(String name) {
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
public void procObstacles(Fish fish) {
  if (frameCount%(getFramesPerObs()) == 0) addObstacle();
  moveObstacles(fish);
}

//add a random obstacle from the correct level in level_entities
public void addObstacle() {
  String name = getRandomName();
  PImage obs_image = resizeObstacle(name);
  obstacles.add(nameToObj(name, obs_image));
}

public void moveObstacles(Fish fish) {
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

public int getFramesPerObs() {
  return PApplet.parseInt(map(level, 1, level_entities.size(), framerate*2, PApplet.parseInt(framerate/2)));
}

//returns a random pixel height that is scaled towards the edges of the screen
//favors players who have the control to keep their character in the middle of the screen
//favors skill
public int getPlaceHeight() {
  int parity = (random(0, 1) > 0.5f) ? -1 : 1;
  float place_height = (random(0, height/2)*parity*1.1f)+height/2;
  if (place_height < getObsH()/2 || height-place_height < getObsH()/2) place_height = getObsH()/2;
  return PApplet.parseInt(place_height);
}

//returns the current obstacle velocity for that level
//first level obstacles take 10 secs to travel across the screen
//final level obstacles take 3 secs to travel across the screen
public float getObsVel() {
  float lowerBound = width*1.0f/framerate/5;
  float upperBound = width*1.0f/framerate/2;
  return -map(level, 1, level_entities.size(), lowerBound, upperBound);
}

//-----------------------------------------------------------------------------------
//Obstacle logic

//returns a level-appropriate random name from an array of entities
public String getRandomName(){
  String[] entities = level_entities.get(level-1);
  return getRandom(entities);
}

//returns a random name from an array of entities
public String getRandom(String[] entities) {
  return entities[PApplet.parseInt(random(0, entities.length))];
}

//takes in the datafile format name and a processed obstacle image
//returns an object of appropriate type
public Obstacle nameToObj(String name, PImage obs_image) {
  if (name.equals("octopus")) return new Octopus(new PVector(width, getPlaceHeight()), obs_image);
  else if (name.equals("electric_eel")) return new ElectricEel(new PVector(width, getPlaceHeight()), obs_image);
  else return new Obstacle(new PVector(width, getPlaceHeight()), obs_image);
}

//--------------------------------------------------------------------------------------
//Obstacle sprite size

//takes in the name of an obstacle is sprites and returns a level-resized copy
public PImage resizeObstacle(String name) {
  int imgH = getObsH();
  int imgW = getPropWidth(sprites.get(name), imgH);
  return resizeImage(sprites.get(name), imgW, imgH);
}

//returns the scaled width depending on an images original h/w ratio
public int getPropWidth(PImage original, int imgH) {
  return PApplet.parseInt(original.width*1.0f/original.height*imgH);
}

//return the current level's obstacle height
public int getObsH() {
  int numLevels = level_entities.size();
  return PApplet.parseInt(map(level, 0, numLevels, height*1.0f/15, height*1.0f/10));
}

//-------------------------------------------------------------------------------------
class Fish extends Object {

  boolean dead;
  boolean showHitBox = false;

  Fish(PVector pos0, PImage image) {
    super(pos0, image);
    dead = false;
  }

  public void checkDeath(ArrayList<Obstacle> obstacles) {
    float r = getImageGeoMean();
    for (Obstacle obstacle : obstacles) {
      float dist = sqrt(pow(getXPos()-obstacle.getXPos(), 2) + pow(getYPos()-obstacle.getYPos(), 2));
      if (dist < (r + obstacle.getImageGeoMean())) dead = true;
    }
  }

  public void move(PVector acc, ArrayList<Obstacle> obstacles) {
    if (!dead) {
      vel.add(acc);
      vel.limit(15);
      pos.add(vel);
      checkYPos();
      draw();
    }
    checkDeath(obstacles);
  }

  //checks if the fish's y position is not off the screen
  //ensures the fish is always playable and not cheaty
  public void checkYPos() {
    if (pos.y < image.height/2) {
        pos.y = image.height/2;
        vel.mult(-0.5f);
      } else if (pos.y > height-image.height/2) {
        pos.y = height-image.height/2;
        vel.mult(-0.5f);
      }
  }

  //resets the fish's position to the vertical middle of the screen
  //resets the fish to alive
  public void reset() {
    pos = new PVector(width/10, height/2);
    dead = false;
  }

  public void draw() {
    if (!dead) {
      super.draw();
      if (showHitBox) showHitBox();
    }
  }
}
class Object {

  PVector pos, vel, acc;
  PImage image;
  boolean showHitBox = false;

  Object(PVector pos0, PImage image) {
    this.pos = pos0;
    this.vel = new PVector(0, 0);
    this.acc = new PVector(0, 0);
    this.image = image;

    if (getYPos() > height-getImageY()/2) pos.y = height-getImageY()/2;
    if (getYPos() < getImageY()/2) pos.y = getImageY()/2;
  }

  public void showHitBox() {
    noFill();
    stroke(255, 0, 0);
    strokeWeight(3);
    ellipseMode(CENTER);
    ellipse(getXPos(), getYPos(), getImageGeoMean()*2, getImageGeoMean()*2);
  }

  public float getXPos() {
    return pos.x;
  }

  public float getYPos() {
    return pos.y;
  }

  public float getImageX() {
    return image.width;
  }

  public float getImageY() {
    return image.height;
  }

  public float getImageGeoMean() {
    return sqrt(getImageX()*getImageY()/4.0f);
  }

  public void draw() {
    imageMode(CENTER);
    image(image, pos.x, pos.y);
    if (showHitBox) showHitBox();
  }

  public void checkYPos() {
    if (getYPos() < getImageY()/2) {
        pos.y = getImageY()/2;
        vel.y *= -0.5f;
      } else if (getYPos() > height-getImageY()/2) {
        pos.y = height-getImageY()/2;
        vel.y *= -0.5f;
      }
  }
}
class Obstacle extends Object {

  boolean passed;
  boolean showHitBox = false;

  Obstacle(PVector pos0, PImage image) {
    super(pos0, image);
    this.passed = false;
  }

  public void move(float imageVel, Fish fish) {
    pos.x += imageVel;
    if (pos.x<-image.width/2) passed = true;
    checkYPos();
    draw();
  }

  public void draw() {
    if (!passed) {
      super.draw();
      if (showHitBox) showHitBox();
    }
  }
}

class Octopus extends Obstacle {

  Octopus(PVector pos0, PImage image) {
    super(pos0, image);
  }

  public void move(float imageVel, Fish fish) {
    acc = new PVector(-0.25f, (fish.getYPos()-getYPos())*1.0f/height);
    vel.add(acc);
    vel.limit(12);
    pos.add(vel);
    super.move(imageVel, fish);
  }
}

class ElectricEel extends Obstacle {

  PImage image1, image2, image3;
  int counter;

  ElectricEel(PVector pos0, PImage image) {
    super(pos0, image);
    counter = 0;
    try {
      image2 = loadImage("electric_eel_2.png");
      image3 = loadImage("electric_eel_3.png");
    }
    catch (Exception e) {
      print("Electric_eel sprites not found");
    }
    int x = PApplet.parseInt(image.width*2.25f);
    int y = PApplet.parseInt(image.height*2.25f);
    image1 = image.copy();
    image1.resize(x, y);
    image2.resize(x, y);
    image3.resize(x, y);
  }

  public void draw() {
    int stage = PApplet.parseInt(counter/10)%5;
    if (stage == 0) image = image1.copy();
    else if (stage <= 2) image = image2.copy();
    else image = image3.copy();
    counter++;
    super.draw();
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "soundFish" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
