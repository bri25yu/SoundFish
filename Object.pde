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

  void showHitBox() {
    noFill();
    stroke(255, 0, 0);
    strokeWeight(3);
    ellipseMode(CENTER);
    ellipse(getXPos(), getYPos(), getImageGeoMean()*2, getImageGeoMean()*2);
  }

  float getXPos() {
    return pos.x;
  }

  float getYPos() {
    return pos.y;
  }

  float getImageX() {
    return image.width;
  }

  float getImageY() {
    return image.height;
  }

  float getImageGeoMean() {
    return sqrt(getImageX()*getImageY()/4.0);
  }

  void draw() {
    imageMode(CENTER);
    image(image, pos.x, pos.y);
    if (showHitBox) showHitBox();
  }

  void checkYPos() {
    if (getYPos() < getImageY()/2) {
        pos.y = getImageY()/2;
        vel.y *= -0.5;
      } else if (getYPos() > height-getImageY()/2) {
        pos.y = height-getImageY()/2;
        vel.y *= -0.5;
      }
  }
}
