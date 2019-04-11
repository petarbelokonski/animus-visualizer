import ddf.minim.*;
import controlP5.*;
import java.util.*;
import processing.video.*;
// import processing.sound.*;
import ddf.minim.analysis.*;


final float PHI = (1.0 + sqrt(5.0)) / 2.0;
final int FONT_SIZE = 14;
final int TEXT_OFFSET = 20;
final int INTERFACE_FADE_RATE = 10;
PShader spriteShader;
PImage sprite;
PImage glow, glowBig, glowBig2;

Minim minim;
AudioInput input;
Visualizer[] visualizers;
int select;
float lastMouseX, lastMouseY, lastMillis;

//Gui
ControlP5 cp5;
VolumeBar volumeBar;
CheckBox[] buttons;
Textlabel[] buttonLabels;
CheckBox highlight, expand, revolve, particles, front, rear, top, autoPan, viewing, blur, invert, ring, fluid, droplet, name, animation;
Textlabel interfaceLabel;
boolean load;
float sliderVal;
PImage logo;
PFont font;
// PageDot[] dots;
boolean showInterface;
boolean debugMode;
float showIntro = 0; // originally at 255, we don't need intro, so set it at 0
float interfaceT;
int contrast;
PImage cam, modeBackground;

boolean showName, showAnimation;

PFont f, nameFont;
String message = "Purple Banana Syndicate Raw Dope Bass 2.";

// Global variables
ArrayList<Particle> particlesme = new ArrayList<Particle>();
int pixelSteps = 6; // Amount of pixels to skip
boolean drawAsPoints = false;
ArrayList<String> words = new ArrayList<String>();
int wordIndex = 0;
color bgColor = color(255, 100);
String fontName = "Arial Bold";

//MyThread thread;
BeatDetect beat;
Movie myMovie;

FFT fft;
float[] spectrum = new float[512];
int increment = 0;
float level;

public void settings() {
  size(displayWidth, displayHeight, P3D);
  fullScreen(P3D, SPAN);
  smooth(8);
}

void setup() {
    minim = new Minim(this); 
    spriteShader = loadShader("spritefrag.glsl", "spritevert.glsl");
    sprite = loadImage("sprite.png");
    glow = loadImage("glow.png");
    glowBig = loadImage("glow_big.png");
    glowBig2 = loadImage("glow_big2.png");
    spriteShader.set("sprite", glow);
    spriteShader.set("sharpness", .9);
    PFont pfont = createFont("Andale Mono.ttf", FONT_SIZE, true);
    ControlFont cFont = new ControlFont(pfont, FONT_SIZE);
    textFont(pfont);
    // showInterface = true;
    Visualizer ring, fluid, droplet;
    logo = loadImage("Logo.png");
    input = minim.getLineIn(Minim.STEREO, 512);
    cam = loadImage("Camera.png");
    modeBackground = loadImage("ModeSelector.png");
    ring = new Ring(input);
    fluid = new Fluid(input);
    droplet = new Droplet(input);
    visualizers = new Visualizer[] {ring, fluid, droplet};
    select = 0;
    frameRate(visualizers[select].getOptimalFrameRate());
    ellipseMode(CENTER);
    ellipseMode(RADIUS);

    // dots = new PageDot[visualizers.length];
    // float dist = 13;
    // for (int i = 0; i < dots.length; i++) {
    //     float w = (dots.length) * dist - (dist / 2);
    //     float dx = (width / 2 - w) + (2 * dist * i + (dist / 2));
    //     dots[i] = new PageDot(dx, height - dist * 2, dist / 2, visualizers[i].name);
    // }
    buttons = new CheckBox[16];
    buttonLabels = new Textlabel[16];
    cp5 = new ControlP5(this);
    guiSetup(cFont);
    visualizers[select].setup();


    background(255, 204, 0);
    
    beat = new BeatDetect(input.bufferSize(), input.sampleRate());

    nameFont = createFont("agency-fb.ttf",256,true);

    myMovie = new Movie(this, "cover5.mov");
    myMovie.loop();
    
    fft = new FFT(input.bufferSize(), input.sampleRate());
    // fft.input(input);

}

/// TODO:
//
// - commit and clean up code
/// - video overlay color when animation 
/// - make video shorter to use less memo
/// - experiment with different video styles /maybe switch between a few of them
/// - full screen mode
/// - make the text go bigger based on equalizer
/// - make the text change position and color based on kick/snare
/// - draw lines positioned randomly with the text which sized also based on equalizer
///
//


void draw() {
    if (showInterface) {
        // interfaceT = lerp(interfaceT, 255, .01);
        if (interfaceT < 255) {
            interfaceT += INTERFACE_FADE_RATE;
            setGuiColors();
        }
        
        // tint(255, (int)interfaceT);
    
        boolean handOn = false;
        if (cp5.isMouseOver()) {
            handOn = true;
        }
        
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisible(true);
        }

        textAlign(CENTER, TOP);
        // fill(255 - visualizers[select].contrast);

        if (debugMode) {
            visualizers[select].displayDebugText();
        }
        if (handOn) {
            cursor(HAND);
        } else {
            cursor(ARROW);
        }
    } else {
        checkMouse();
        // interfaceT = lerp(interfaceT, 0, .2);
        if (interfaceT > 0) {
            interfaceT -= INTERFACE_FADE_RATE;
            setGuiColors();
        } else {
            setInterfaceVisibility(false);
        }

         for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisible(false);
        }

        // tint(255, (int)interfaceT);
    }


  if (showAnimation) {
     image(myMovie, 0, 0, width, height);

        pushStyle();
        pushMatrix();
        visualizers[select].retrieveSound();
        // strokeCap(ROUND);
        // shader(spriteShader, POINTS);
        visualizers[select].draw();
        blendMode(BLEND);
        popMatrix();
        popStyle();
        noLights();
        updateGui();
        contrast = visualizers[select].contrast;
    } 

    volumeBar.visible = showInterface;

    if (visualizers[select].sampleParticleMode) {
        float avgFr = visualizers[select].sampleFrameRate();
        if (avgFr > 0) {
            visualizers[select].adjustDetail(avgFr);
        }
    }

    if (showName) {

    background(0);
    stroke(255);

    image(myMovie, 0, 0, width, height);
  
    //     int x = 10;

    //     for (int i = 0; i < message.length(); i++) {  
    //       textFont(nameFont, 66+50*input.mix.get(i));
    //       text(message.charAt(i),400+x,height/2);
    //        // textWidth() spaces the characters out properly.
    //        x += textWidth(message.charAt(i)); 
    //     }
            
    
            
            // textFont(nameFont, 150);
                       textFont(nameFont, 100+100*input.mix.level());

            
            beat.detect(input.mix);
            textAlign(CENTER);
//   draw the waveforms so we can see what we are monitoring
    // print(input.bufferSize());
//    for(int i = 0; i < input.bufferSize()-1; i+=32) {

        //  line( i, 50 + input.left.get(i)*50, i+1, 50 + input.left.get(i+1)*50 );
        //     line( i, 150 + input.right.get(i)*50, i+1, 150 + input.right.get(i+1)*50 );
            // if(i%31==1) {
        
            if (increment%8==0){
                fill(255,255,255); 
                
            text("ARKS", displayWidth*1/2, displayHeight*1/2);
            }
             else{ text("ARKS", displayWidth*1/2, displayHeight*1/2);
            }

             if(increment%32==0){
                // textFont(nameFont, displayWidth);
                fill(15,18,51); 
                // fill(input.mix.get(increment)*200+20, input.mix.get(250)*100+15, 50);  

                text("ARKS", displayWidth*1/2, displayHeight*1/2);
            }
            

            level = input.mix.level();

             if (level>0.1 && level<0.2){
                textFont(nameFont, displayWidth/4);
                fill(255,255,255,57);  

                text("ARKS", displayWidth*1/2, displayHeight*1/2);

            }

             if (level>0.2 && level<0.3){
                textFont(nameFont, displayWidth/2);
                fill(0,0,0,57);  

                pushMatrix();
                scale(1, -1);
                text("ARKS", displayWidth*1/2, displayHeight*1/2);
                popMatrix();
            }
 
            if (level>0.4 && level<0.5){
                textFont(nameFont, displayWidth/12);
                fill(255,255,255,57);  

                
                translate(displayWidth,displayHeight);
                pushMatrix();
                scale(1, -1);
                text("ARKS", displayWidth*1/2, displayHeight/2);
                popMatrix();
            }

            increment+=2;
            if (increment >= input.bufferSize() ){
                increment = 0;
            } 
            
//    }
//     //   textFont(nameFont, 66+input.left.get(i)*2);
//      print(input.mix.get(i));

//         if (input.left.get(i)%8==1) {

       

            // fill(255,255,255);
            // if (beat.isKick()) {
            //     fill(10, 50, 50);  
            //     text("Purple Banana Syndicate", displayWidth*1/2, displayHeight*1/2);
                
            // } else if (beat.isSnare()) {
            //     fill(input.mix.level()*110, 100, 50);  
            //     text("Purple Banana Syndicate", displayWidth*1/15, displayHeight*1/3+random(-1,1)*1400);
            // } else {
            //     fill(218, 150, 150);  
            //     text("Purple Banana Syndicate", displayWidth*1/17, displayHeight*1/2+random(-200,300));
                
            // }       

        // } 
// displayWidth, displayHeight
    //  line( i, 50 + input.left.get(i)*50, i+1, 50 + input.left.get(i+1)*50 );
    //  line( i, 150 + input.right.get(i)*50, i+1, 150 + input.right.get(i+1)*50 );
//   }


// text("Purple Banana Syndicate", 400+input.left.get(i)*2, 0);

        // fft.forward(input.mix);

        // for (int i = 0; i < fft.specSize(); i++) {
        //     textFont(nameFont, 66 + fft.getBand(i));
        //     // text("Purple Banana Syndicate", 400, 0);   
        //     line(i, height, i, height - fft.getBand(i) * 4);
        //     // line(i, height, i, height - fft.getBand(i) * 4);
        // }

        // image(myMovie, 0, 0, width, height);

        // beat.detect(input.mix);
        // textAlign(CENTER, TOP);
        // fill(255,255,255);
        // if (beat.isKick()) {
            // textFont(nameFont, 66);
            // text("Purple Banana Syndicate", 400+random(12,36), 0);    
        //     fill(151,103,134);
        //     text("Purple Banana Syndicate", 400+random(12,36), 0);    
        //     fill(111,13,34);
        //     text("Purple Banana Syndicate", 400+random(12,36), random(12,36));    
        // } else if (beat.isSnare()) {

        

        //     textFont(nameFont, 26);
        //     text("Purple Banana Syndicate", 400-random(12,36), 0);
        // } else {
        //     textFont(nameFont, 36);
        //     text("Purple Banana Syndicate", 400, 0); 
        // }
        // //textFont(f);         
        // int x = 10;
        // for (int i = 0; i < message.length(); i++) {
        //    textSize(random(12,36));
        //    text(message.charAt(i),x,height/2);
        //    // textWidth() spaces the characters out properly.
        //    x += textWidth(message.charAt(i)); 
        // }
        ////noLoop()
        
           // Background & motion blur
        //fill(bgColor);
        //noStroke();
        //rect(0, 0, width*2, height*2);
        noLights();
        updateGui();
    }

  
}

// Called every time a new frame is available to read
void movieEvent(Movie m) {
  m.read();
}


// void mousePressed() {
//     for (int i = 0; i < dots.length; i++) {
//         if (dots[i].overDot) {
//             select = i;
//             switchVisualizer();
//             break;
//         }
//     }        
// }

void checkMouse() {
    if (mouseX != lastMouseX && mouseY != lastMouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastMillis = millis();
        cursor(ARROW);
    } else if (millis() - lastMillis > 1500) {
        noCursor();
    } 
}

void switchVisualizer() {
    visualizers[select].setup();
    frameRate(visualizers[select].getOptimalFrameRate());
    setGuiColors();
}

void updateGui() {
    float[] on = new float[]{1};
    float[] off = new float[]{0};
    buttons[0].setArrayValue(visualizers[select].highlight ? on : off);
    buttons[1].setArrayValue(visualizers[select].expand ? on : off);
    buttons[2].setArrayValue(visualizers[select].revolve ? on : off);
    buttons[3].setArrayValue(visualizers[select].particles ? on : off);
    buttons[4].setArrayValue(visualizers[select].frontView ? on : off);
    buttons[5].setArrayValue(visualizers[select].rearView ? on : off);
    buttons[6].setArrayValue(visualizers[select].topView ? on : off);
    buttons[7].setArrayValue(visualizers[select].camera.autoPanningMode ? on : off);
    buttons[8].setArrayValue(visualizers[select].followMouse ? on : off);
    buttons[9].setArrayValue(visualizers[select].blur ? on : off);
    buttons[10].setArrayValue(visualizers[select].contrast == 255 ? on : off);
    buttons[11].setArrayValue(select == 0 ? on : off);
    buttons[12].setArrayValue(select == 1 ? on : off);
    buttons[13].setArrayValue(select == 2 ? on : off);

    // image(loadImage("Button.png"), mouseX, mouseY);
    // if(mousePressed){
    //     println(mouseX + " " + mouseY);
    // }
}

void guiSetup(ControlFont font){
    volumeBar = new VolumeBar(width - (212), TEXT_OFFSET+69, "VolumeBackground.png", "VolumeMid.png", "VolumeEnd.png", "VolumeBackgroundI.png", "VolumeMidI.png", "VolumeEndI.png");
    interfaceLabel = cp5.addTextlabel("label")
            .setText("Press [h] To Hide Interface")
            .setFont(font)
            .setPosition(width - 230-15, TEXT_OFFSET);
    interfaceLabel.getCaptionLabel().setSize(FONT_SIZE);

    buttons[0] = highlight = cp5.addCheckBox("highlight").addItem("highlight [1]", 0);
    buttonLabels[0] = cp5.addTextlabel("highlightT").setText("Highlight [1]");
    buttons[1] = expand = cp5.addCheckBox("expand").addItem("expand [2]", 0);
    buttonLabels[1] = cp5.addTextlabel("expandT").setText("Expand [2]");
    buttons[2] = revolve = cp5.addCheckBox("revolve").addItem("revolve [3]", 0);
    buttonLabels[2] = cp5.addTextlabel("revolveT").setText("Revolve [3]");
    buttons[3] = particles = cp5.addCheckBox("particles").addItem("particles [4]", 0);
    buttonLabels[3] = cp5.addTextlabel("particlesT").setText("Particles [4]");
    buttons[4] = front = cp5.addCheckBox("front").addItem("front view [f]", 0);
    buttonLabels[4] = cp5.addTextlabel("frontT").setText("a");
    buttons[5] = rear = cp5.addCheckBox("rear").addItem("rear view [r]", 0);
    buttonLabels[5] = cp5.addTextlabel("rearT").setText("d");
    buttons[6] = top = cp5.addCheckBox("top").addItem("top view [t]" , 0);
    buttonLabels[6] = cp5.addTextlabel("topT").setText("w");
    buttons[7] = autoPan = cp5.addCheckBox("autoPan").addItem("autopan camera [a]", 0);
    buttonLabels[7] = cp5.addTextlabel("autoPanT").setText("s");
    buttons[8] = viewing = cp5.addCheckBox("viewing").addItem("follow mouse [m]", 0);
    buttonLabels[8] = cp5.addTextlabel("viewingT").setText("Follow Mouse [m]");
    buttons[9] = blur = cp5.addCheckBox("blur").addItem("blur [b]", 0);
    buttonLabels[9] = cp5.addTextlabel("blurT").setText("Blur [b]");
    buttons[10] = invert = cp5.addCheckBox("invert").addItem("invert [i]", 0);
    buttonLabels[10] = cp5.addTextlabel("inbertT").setText("Invert [i]");
    buttons[11] = ring = cp5.addCheckBox("ring").addItem("Ring", 0);
    buttonLabels[11] = cp5.addTextlabel("Mode").setText("Mode");
    buttons[12] = fluid = cp5.addCheckBox("fluid").addItem("Fluid", 0);
    buttonLabels[12] = cp5.addTextlabel("Sensitivity").setText("Mic Sensitivity");
    buttons[13] = droplet = cp5.addCheckBox("droplet").addItem("Droplet", 0);
    buttonLabels[13] = cp5.addTextlabel("name").setText(visualizers[select].name);
    buttons[14] = name = cp5.addCheckBox("name").addItem("name [n]", 0);
    buttonLabels[14] = cp5.addTextlabel("nameT").setText("Name [n]");
    buttons[15] = animation = cp5.addCheckBox("animation").addItem("animation [v]", 0);
    buttonLabels[15] = cp5.addTextlabel("animationT").setText("Animation [v]");

    
    float startHeight = TEXT_OFFSET + 92;
    PImage normal = loadImage("Button.png");
    PImage hover = loadImage("Button.png");
    PImage click = loadImage("ButtonPressed.png");
    for (int i = 0; i < buttons.length; i++) {
        if (i == 4) {
            startHeight = TEXT_OFFSET + 30;
        } else if (i == 9) {
            startHeight = TEXT_OFFSET + 70;
        }
        buttonLabels[i].setPosition(width - (212 - 28), int(startHeight + 5 + (1 + i) * 28))
            .setFont(font);
        buttons[i].setPosition(width - 212, startHeight + (1 + i) * 28)
            .setImages(normal, hover, click)
            .setSize(23, 23)
            .getCaptionLabel().setFont(font).setSize(FONT_SIZE);
            buttons[i].getItem(0).getCaptionLabel().setFont(font).setSize(FONT_SIZE);
    }
    buttons[4].setPosition(width - 180, startHeight + 196); //front
    buttonLabels[4].setPosition(width - 177, startHeight + 201); //front
    buttons[5].setPosition(width - 114, startHeight + 196); //rear
    buttonLabels[5].setPosition(width - 111, startHeight + 201); //rear
    buttons[6].setPosition(width - 147, startHeight + 166); //top
    buttonLabels[6].setPosition(width - 144, startHeight + 171); //top
    buttons[7].setPosition(width - 147, startHeight + 226); //autoPan
    buttonLabels[7].setPosition(width - 144, startHeight + 231); //autoPan
    buttons[8].setPosition(width - 180, startHeight + 252); 
    buttons[11].setPosition(width - 180, TEXT_OFFSET + 23); //ring
    buttons[12].setPosition(width - 147, TEXT_OFFSET + 23); //fluid
    buttons[13].setPosition(width - 114, TEXT_OFFSET + 23); //droplet
    
    buttonLabels[8].setPosition(width - (180 - 28), startHeight + 257);
    buttonLabels[11].setPosition(width - (212 - 58), startHeight - 20);
    buttonLabels[12].setPosition(width - (212 - 12), startHeight + 26);
    buttonLabels[13].setPosition(displayWidth / 2 - 25, TEXT_OFFSET);
    setGuiColors();
}

void setGuiColors() {
    interfaceT = visualizers[select].bindRange(interfaceT, 0.0, 255.0);
    int textColor = (int) abs(visualizers[select].contrast - interfaceT);
    buttonLabels[13].setText(visualizers[select].name);
    buttonLabels[13].setPosition(displayWidth / 2 - 25, TEXT_OFFSET);
    for(int i = 0; i < buttonLabels.length; i++) {
        // buttonLabels[i].setColor(color(255 - visualizers[select].contrast));
        if (i < 4 || i > 7) {// don't reverse camera keys
            buttonLabels[i].setColor(textColor);
        }
    }
    // interfaceLabel.setColor(color(255 - visualizers[select].contrast));
    // println("orig: " + (255 - visualizers[select].contrast) + ", interfaceT: " + interfaceT);
    interfaceLabel.setColor(textColor);
    volumeBar.invert = visualizers[select].contrast == 255 ? true: false;
}

void controlEvent(ControlEvent theEvent) {
    if (theEvent.isFrom(highlight)) {
        visualizers[select].highlight();
    } else if (theEvent.isFrom(expand)) {
        visualizers[select].expand();
    } else if (theEvent.isFrom(revolve)) {
        visualizers[select].revolve();
    } else if (theEvent.isFrom(particles)) {
        visualizers[select].particles();
    } else if (theEvent.isFrom(front)) {
        visualizers[select].fPressed();
    } else if (theEvent.isFrom(rear)) {
        visualizers[select].rPressed();
    } else if (theEvent.isFrom(top)) {
        visualizers[select].tPressed();
    } else if (theEvent.isFrom(autoPan)) {
        visualizers[select].aPressed();
    } else if (theEvent.isFrom(viewing)) {
        visualizers[select].mPressed();
    } else if (theEvent.isFrom(blur)) {
        visualizers[select].blur = !visualizers[select].blur;
    } else if (theEvent.isFrom(invert)) {
        visualizers[select].contrast = 255 - visualizers[select].contrast;
        setGuiColors();
    } else if (theEvent.isFrom(ring)) {
        select = 0;
    } else if (theEvent.isFrom(fluid)) {
        select = 1;
    } else if (theEvent.isFrom(droplet)) {
        select = 2;
    } else if (theEvent.isFrom(name)) {
        // TODO: show on // off name
        showName = !showName;
        if (showName == true) {
        }
    } else if (theEvent.isFrom(animation)) {
        showAnimation = !showAnimation;
    }
}

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

void keyPressed() {
    switch (key) {
        case 'D':
            debugMode = !debugMode;
            break;
        case 'h':
            println("h pressed");
            showInterface = !showInterface;
            if (showInterface) {
                setInterfaceVisibility(true);
            }
            break;         
        case 'i':
            visualizers[select].contrast = 255 - visualizers[select].contrast;
            setGuiColors();
            break;
        case 'I':
            visualizers[select].contrast = 255 - visualizers[select].contrast;
            setGuiColors();
            break;            
        default:
            break;
    }
    switch (keyCode) {
        case 37: // left arrow key
            select--;
            if (select < 0) {
                select = visualizers.length - 1;
            }
            switchVisualizer();
            break;
        case 39: // right arrow key
            select++;
            select %= visualizers.length;
            switchVisualizer();
            break;
        default:
            break;
    }
    visualizers[select].keyPressed();
}

void setInterfaceVisibility(boolean val) {
    for (int i = 0; i < buttonLabels.length; i++) {
        buttonLabels[i].setVisible(val);
    }
    interfaceLabel.setVisible(val);
}

void stop() {
    input.close();
    minim.stop();
    super.stop();
}
