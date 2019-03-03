class Obstacle extends Object {

  boolean passed;
  boolean showHitBox = false;

  Obstacle(PVector pos0, PImage image) {
    super(pos0, image);
    this.passed = false;
  }

  void move(float imageVel, Fish fish) {
    pos.x += imageVel;
    if (pos.x<-image.width/2) passed = true;
    checkYPos();
    draw();
  }

  void draw() {
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

  void move(float imageVel, Fish fish) {
    acc = new PVector(-0.25, (fish.getYPos()-getYPos())*1.0/height);
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
    int x = int(image.width*2.25);
    int y = int(image.height*2.25);
    image1 = image.copy();
    image1.resize(x, y);
    image2.resize(x, y);
    image3.resize(x, y);
  }

  void draw() {
    int stage = int(counter/10)%5;
    if (stage == 0) image = image1.copy();
    else if (stage <= 2) image = image2.copy();
    else image = image3.copy();
    counter++;
    super.draw();
  }
}