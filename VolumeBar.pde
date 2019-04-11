class VolumeBar {
    int x;
    int y;
    float value;
    PImage backgroundImg;
    PImage midSection;
    PImage end;
    PImage backgroundImgI;
    PImage midSectionI;
    PImage endI;
    int size;
    boolean visible;
    boolean invert;
    
    VolumeBar(int x, int y, String backgroundImg, String midSection, String end, String backgroundImgI, String midSectionI, String endI) {
        this.x = x;
        this.y = y; 
        this.backgroundImg = loadImage(backgroundImg);
        this.midSection = loadImage(midSection);
        this.end = loadImage(end);
        this.backgroundImgI = loadImage(backgroundImgI);
        this.midSectionI = loadImage(midSectionI);
        this.endI = loadImage(endI);
        value = 0.5;
        visible = true;
    }
    
    //Visible is not Normal, The GUI handels showing/hiding images
    void update() {
        if(invert) {
            image(backgroundImgI, x, y);
        } else {
            image(backgroundImg, x, y);
        }
        size = size >= 136 ? 136: size;
        size = size <= 10 ? 10: size;
        size = round(lerp(size, round(value * backgroundImg.width) - 9, .2));

        for(int i = 0; i < size-end.width; i+=midSection.width) {
            if(invert) {
                image(midSectionI, int(this.x+11 + i), this.y);
            } else {
                image(midSection, int(this.x+11 + i), this.y);
            }
        }
        if(invert) {
            image(endI, this.x+size+4, this.y);
        } else {
            image(end, this.x+size+4, this.y);
        }
        if(visible) {
            if(mousePressed) {
              if(mouseX >= this.x && mouseX < this.x + this.backgroundImg.width &&
               mouseY >= this.y && mouseY < this.y + this.backgroundImg.height) {
                   value = map(mouseX - this.x, 0, this.backgroundImg.width, 0, 1);
               }
            }
        }
    }
}