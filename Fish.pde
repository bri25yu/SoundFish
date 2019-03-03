class Fish extends Object {

  boolean dead;
  boolean showHitBox = false;

  Fish(PVector pos0, PImage image) {
    super(pos0, image);
    dead = false;
  }

  void checkDeath(ArrayList<Obstacle> obstacles) {
    float r = getImageGeoMean();
    for (Obstacle obstacle : obstacles) {
      float dist = sqrt(pow(getXPos()-obstacle.getXPos(), 2) + pow(getYPos()-obstacle.getYPos(), 2));
      if (dist < (r + obstacle.getImageGeoMean())) dead = true;
    }
  }

  void move(PVector acc, ArrayList<Obstacle> obstacles) {
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
  void checkYPos() {
    if (pos.y < image.height/2) {
        pos.y = image.height/2;
        vel.mult(-0.5);
      } else if (pos.y > height-image.height/2) {
        pos.y = height-image.height/2;
        vel.mult(-0.5);
      }
  }

  //resets the fish's position to the vertical middle of the screen
  //resets the fish to alive
  void reset() {
    pos = new PVector(width/10, height/2);
    dead = false;
  }

  void draw() {
    if (!dead) {
      super.draw();
      if (showHitBox) showHitBox();
    }
  }
}
