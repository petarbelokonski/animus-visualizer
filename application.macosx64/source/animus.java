import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import controlP5.*; 
import java.util.*; 
import processing.video.*; 
import ddf.minim.analysis.*; 
import ddf.minim.*; 
import ddf.minim.*; 
import ddf.minim.*; 
import ddf.minim.*; 
import java.util.*; 
import java.io.*; 
import ddf.minim.analysis.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class animus extends PApplet {

public class ColorTracker {
    float deltaMax;
    float deltaMin;
    
    float red, green, blue;
    boolean incrRed, incrGreen, incrBlue;
    float dr, dg, db;

    ColorTracker(float redStart, float greenStart, float blueStart, float deltaMin, float deltaMax) {
        this.deltaMin = deltaMin;
        this.deltaMax = deltaMax;
        incrRed = true;
        incrBlue = false;
        incrGreen = false;
        red = redStart;
        green = greenStart;
        blue = blueStart;
        pickRandomDeltas();
    }    
    
    ColorTracker(float deltaMin, float deltaMax) {
        this(random(125, 255), random(0, 125), random(67, 200), deltaMin, deltaMax);
    }
    
    public void pickRandomDeltas() {
        dr = random(deltaMin, deltaMax);
        dg = random(deltaMin, deltaMax);
        db = random(deltaMin, deltaMax);
    }
    
    //call each frame to slowly change colors over time
    public void incrementColor() {
        if (red + blue + green < 255) {
            incrRed = true;
            incrBlue = true;
            incrGreen = true;
            pickRandomDeltas();
            
        } else if (red + blue + green > (255 * 2)) {
            incrRed = false;
            incrBlue = false;
            incrGreen = false; 
            pickRandomDeltas();
        }
        
        if (red > 255) {
            incrRed = false;
            dr = random(deltaMin, deltaMax);
        }
        if (blue > 255) {
            incrBlue = false;
            db = random(deltaMin, deltaMax);
        }
        if (green > 255) {
            incrGreen = false;
            dg = random(deltaMin, deltaMax);
        }
        if (red < 0) incrRed = true;
        if (blue < 0) incrBlue = true;
        if (green < 0) incrGreen = true;    
        
        if (incrRed) red += dr;
            else red -= dr;
        if (incrBlue) blue += db;
            else blue -= db;
        if (incrGreen) green += dg;
            else green -= dg;
    }
    
    public void pickRandomColor() {
        red = random(0, 255);
        green = random(0, 255);
        blue = random(0, 255);    
    }
    
    public void defineLights() {
        lightSpecular(red / 15, red / 15, red / 15);

        directionalLight(0, green / 8, blue / 4, 
                1, 0, 0);
        pointLight(min(red*2, 255), green / 4, blue / 4,
                200, -150, 0);

        pointLight(0, 0, blue,
                0, 150, 200);

        spotLight(255 - red, 255 - (green / 4), 255 - (blue / 4),
                0, 40, 200,
                0, -0.5f, -0.5f,
                PI/2, 1);

        directionalLight(0, 0, 0,
                -1, 0, 0);

    }
}




// import processing.sound.*;



final float PHI = (1.0f + sqrt(5.0f)) / 2.0f;
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
int bgColor = color(255, 100);
String fontName = "Arial Bold";

//MyThread thread;
BeatDetect beat;
Movie myMovie;

FFT fft;
float[] spectrum = new float[512];
int increment = 0;
int two = 0;
float level;

public void settings() {
  size(displayWidth, displayHeight, P3D);
  smooth(8);
}

public void setup() {
    minim = new Minim(this); 
    spriteShader = loadShader("spritefrag.glsl", "spritevert.glsl");
    sprite = loadImage("sprite.png");
    glow = loadImage("glow.png");
    glowBig = loadImage("glow_big.png");
    glowBig2 = loadImage("glow_big2.png");
    spriteShader.set("sprite", glow);
    spriteShader.set("sharpness", .9f);
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


public void draw() {
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

             if (level>0.1f && level<0.2f){
                textFont(nameFont, displayWidth/4);
                fill(255,255,255,57);  

                text("ARKS", displayWidth*1/2, displayHeight*1/2);

            }

             if (level>0.2f && level<0.3f){
                textFont(nameFont, displayWidth/2);
                fill(0,0,0,57);  

                pushMatrix();
                scale(1, -1);
                text("ARKS", displayWidth*1/2, displayHeight*1/2);
                popMatrix();
            }
 
            if (level>0.4f && level<0.5f){
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
public void movieEvent(Movie m) {
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

public void checkMouse() {
    if (mouseX != lastMouseX && mouseY != lastMouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastMillis = millis();
        cursor(ARROW);
    } else if (millis() - lastMillis > 1500) {
        noCursor();
    } 
}

public void switchVisualizer() {
    visualizers[select].setup();
    frameRate(visualizers[select].getOptimalFrameRate());
    setGuiColors();
}

public void updateGui() {
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

public void guiSetup(ControlFont font){
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
        buttonLabels[i].setPosition(width - (212 - 28), PApplet.parseInt(startHeight + 5 + (1 + i) * 28))
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

public void setGuiColors() {
    interfaceT = visualizers[select].bindRange(interfaceT, 0.0f, 255.0f);
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

public void controlEvent(ControlEvent theEvent) {
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
        value = 0.5f;
        visible = true;
    }
    
    //Visible is not Normal, The GUI handels showing/hiding images
    public void update() {
        if(invert) {
            image(backgroundImgI, x, y);
        } else {
            image(backgroundImg, x, y);
        }
        size = size >= 136 ? 136: size;
        size = size <= 10 ? 10: size;
        size = round(lerp(size, round(value * backgroundImg.width) - 9, .2f));

        for(int i = 0; i < size-end.width; i+=midSection.width) {
            if(invert) {
                image(midSectionI, PApplet.parseInt(this.x+11 + i), this.y);
            } else {
                image(midSection, PApplet.parseInt(this.x+11 + i), this.y);
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

public void keyPressed() {
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

public void setInterfaceVisibility(boolean val) {
    for (int i = 0; i < buttonLabels.length; i++) {
        buttonLabels[i].setVisible(val);
    }
    interfaceLabel.setVisible(val);
}

public void stop() {
    input.close();
    minim.stop();
    super.stop();
}
public class Camera {
    PVector pos; //current position of camera
    PVector center; //center view for camera
    PVector dir; //"up" of camera;
    
    PVector moveStart; //saves the initial panning coordinates
    PVector moveEnd; //ending panning coordinates
    boolean movingCamera; //boolean storing whether camera is panning
    int moveTime; //total time to move from moveStart to moveEnd
    int currentTime; //current time while panning
    
    PVector dirStart;
    PVector dirEnd;
    boolean movingDir;
    int mDirTime;
    int mDirCurrTime;
    
    PVector mCenterStart;
    PVector mCenterEnd;
    boolean movingCenter;
    int mCenterTime;
    int mCenterCurrTime;
    
    boolean viewingMode; //true: free panning on
    boolean autoPanningMode; //true: auto panning on
    boolean autoDirChangeMode;
    
    PVector leftOuterBounds; //leftmost x, y, z auto panning bounds (generally negative)
    PVector rightOuterBounds; //rightmost x, y, z auto panning bounds (generally positive)
    PVector leftInnerBounds;
    PVector rightInnerBounds;
    
    Camera(float initX, float initY, float initZ) {
        pos = new PVector(initX, initY, initZ);
        dir = new PVector(0, 1, 0);
        center = new PVector(0, 0, 0);
        moveStart = new PVector(width/2, height/2, height/2);
        moveEnd = new PVector(width/2, height/2, height/2);
        movingCamera = false;
        autoPanningMode = false;
        leftOuterBounds = new PVector(-2000, -2000, -2000);
        rightOuterBounds = new PVector(2000, 2000, 2000);
        leftInnerBounds = new PVector(0, 0, 0);
        rightInnerBounds = new PVector(0, 0, 0);
        viewingMode = true;
        mCenterStart = new PVector(0, 0, 0);
        mCenterEnd = new PVector(0, 0, 0);
        dirStart = new PVector(0, 0, 0);
        dirEnd = new PVector(0, 0, 0);
        movingCenter = false;
        autoDirChangeMode = false;
    }
    
    Camera() {
        this(width/2, height/2, height/2);
    }
    
    public void setCenter(float cx, float cy, float cz) {
        center = new PVector(cx, cy, cz);  
    }
    
    public void setOuterBounds(float lx, float ly, float lz, float rx, float ry, float rz) {
        leftOuterBounds = new PVector(lx, ly, lz);
        rightOuterBounds = new PVector(rx, ry, rz);
    }
    
    public void setInnerBounds(float lx, float ly, float lz, float rx, float ry, float rz) {
        leftInnerBounds = new PVector(lx, ly, lz);
        rightInnerBounds = new PVector(rx, ry, rz);
    }
    
    //switches autoPanningMode on/off, also turns viewingMode off
    public void autoPanSwitch() {
        autoPanningMode = !autoPanningMode;
        viewingMode = false;
    }
    
    //switches dir on/off, also turns viewingMode off
    public void dirSwitch() {
        autoDirChangeMode = !autoDirChangeMode;
        viewingMode = false;
    }
    
    //switches viewingMode on/off, also turns autoPanningMode off
    public void viewSwitch() {
        viewingMode = !viewingMode;
        if (viewingMode) {  
            disableAllModes();
            viewingMode = true;
        } else {
            disableAllModes();
        }
    }
    
    // disables and mode that is affecting camera movement / orientation
    public void disableAllModes() {
        viewingMode = false;
        autoPanningMode = false;
        autoDirChangeMode = false;
        movingCamera = false;
        movingDir = false;
        movingCenter = false;    
    }
    
    //pans camera to set destination at set time (100 apprx. equals 2 seconds)
    public void initMoveCamera(PVector destination, int time) {
        moveStart.x = pos.x;
        moveStart.y = pos.y;
        moveStart.z = pos.z;
        moveEnd = destination;
        moveTime = time;
        currentTime = 0;
        
        movingCamera = true;    
    }
    
    
    public void initMoveDir(PVector destination, int time) {
        dirStart.x = dir.x;
        dirStart.y = dir.y;
        dirStart.z = dir.z;
        dirEnd = destination;
        mDirTime = time;
        mDirCurrTime = 0;
        
        movingDir = true; 
    }
    
    public void initMoveCenter(float dx, float dy, float dz, int time) {
        mCenterStart.x = center.x;
        mCenterStart.y = center.y;
        mCenterStart.z = center.z;
        mCenterEnd = new PVector(dx, dy, dz);
        mCenterTime = time;
        mCenterCurrTime = 0;
  
        movingCenter = true;      
    }
    
    public void rotateCamera(float angleInc) {
        dir.rotate(angleInc);
    }
    
    public PVector pickRandomPoint() {
        float xf, yf, zf;
        float x1 = random(leftOuterBounds.x, leftInnerBounds.x);
        float x2 = random(rightInnerBounds.x, rightOuterBounds.x);
        if (random(1) > 0.5f)
            xf = x1;
        else
            xf = x2;
//            
//        float y1 = random(leftOuterBounds.y, leftInnerBounds.y);
//        float y2 = random(rightInnerBounds.y, rightOuterBounds.y);
//        if (random(1) > 0.5)
//            yf = y1;
//        else
//            yf = y2;
        yf = random(leftOuterBounds.y, rightOuterBounds.y);
            
        float z1 = random(leftOuterBounds.z, leftInnerBounds.z);
        float z2 = random(rightInnerBounds.z, rightOuterBounds.z);
        if (random(1) > 0.5f)
            zf = z1;
        else
            zf = z2;
//        xf = random(leftOuterBounds.x, rightOuterBounds.x);
//        yf = random(leftOuterBounds.y, rightOuterBounds.y);
//        zf = random(leftOuterBounds.z, rightOuterBounds.z);

        return new PVector(xf, yf, zf);           
    }
    
    public void integrate(int currentTime, int maxTime, PVector v, PVector start, PVector end) {
        float angle = (currentTime*1.0f / maxTime) * PI;

        float xAmp = ((end.x - start.x) * PI) / (2 * maxTime);
        float dx = xAmp*sin(angle);
        v.x += dx;
        
        float yAmp = ((end.y - start.y) * PI) / (2 * maxTime);
        float dy = yAmp*sin(angle);
        v.y += dy;
        
        float zAmp = ((end.z - start.z) * PI) / (2 * maxTime);
        float dz = zAmp*sin(angle);
        v.z += dz;
    }

    //must be called every frame
    public void update() {        
        if (viewingMode) {
            pos.x = map(mouseX, 0, width, leftOuterBounds.x, rightOuterBounds.x);
            pos.y = map(mouseY, 0, height, leftOuterBounds.y, rightOuterBounds.y);
        }
        
        if (autoPanningMode && !movingCamera) {
            int time = (int)random(frameRate*8, frameRate*12);
            PVector nextPos = pickRandomPoint();
            initMoveCamera(nextPos, time);
        }
        
        if (autoDirChangeMode && !movingDir) {
            int time;
            if (!autoPanningMode) {
                time = (int)random(frameRate*8, frameRate*12);    
            } else {
                time = moveTime;
            }
            float x = random(-1, 1);
            float y = random(-1, 1);
            float z = random(-1, 1);
            initMoveDir(new PVector(x, y, z), time);
        }
        
        if (movingCamera) {
            integrate(currentTime, moveTime, pos, moveStart, moveEnd);
            currentTime++;
            if (currentTime == moveTime) {
                movingCamera = false;    
            } 
        }
        
        if (movingDir) {
            integrate(mDirCurrTime, mDirTime, dir, dirStart, dirEnd);
            mDirCurrTime++;
            if (mDirCurrTime == mDirTime) {
                movingDir = false;
            } 
        }
        
        if (movingCenter) {
            integrate(mCenterCurrTime, mCenterTime, center, mCenterStart, mCenterEnd);
            mCenterCurrTime++;
            if (mCenterCurrTime == mCenterTime) {
                movingCenter = false;    
            } 
        }
        
        camera(pos.x, pos.y, pos.z, center.x, center.y, center.z, dir.x, dir.y, dir.z);
    }
    
}


class Droplet extends Visualizer {
    public @Override
    int getOptimalFrameRate() {
        return 40;
    }
 
    final int SPEC_SIZE = 50;
    final int SPEC_WIDTH = 7;
    final int DETAIL = 6;
    final int PART_DETAIL = 12;
    final float DECAY = 0.25f; // DECAY = -y per frame
    final int MAX_DECAY = 100;
    final int PEAK = 40;
    final float EXPAND_RATE = 0.02f;
    final float HIGHLIGHT_POINT_STOP = 80;
    final float MIN_PART_SIZE = 2;
    final float MAX_PART_SIZE = 20;
    final float PART_SCALE = 0.5f;
    final int MAX_DROPLET_SIZE = 4;

    int particleDetail = -1;
    int dropletSize = MAX_DROPLET_SIZE;
    float dropletXRot, dropletYRot;
    
    float currExpand = 0;

    // since we need 4 different color trackers -- base and peak colors for both
    // bottom and top halves -- stored all dem in an array
    // colorTrackers[0] -> base tracker for bottom half
    // colorTrackers[1] -> peak tracker for bottom half
    // colorTrackers[2] -> base tracker for top half
    // colorTrackers[3] -> peak tracker for top half
    ColorTracker[] colorTrackers;
    
    Ring[] rings;
    RotationTracker rotater;

    Droplet(AudioInput input) {
        super(input, "DROPLET");
        camera.pos = new PVector(-350, 0, .0001f);
        float n = SPEC_SIZE * SPEC_WIDTH;
        camera.setOuterBounds(-n, -n * 1.2f, -n, n, n * 1.2f, n);
        camera.setInnerBounds(-n / 4, 0, - n / 4, n / 4, 0, n / 4);
        camera.viewSwitch();
        colorTrackers = new ColorTracker[4];
        for (int i = 0; i < colorTrackers.length; i++) {
            colorTrackers[i] = new ColorTracker(0.5f, 4);
        }
        rotater = new RotationTracker();
        rings = new Ring[SPEC_SIZE];
        setupDroplet();
        aPressed();
    }
    
    public void setupDroplet() {
        // int detail = (particles) ? PART_DETAIL : DETAIL;

        for (int i = 0; i < rings.length; i++) {
            int radius = SPEC_WIDTH * (i + 1);
            // int pointNum = (particles) ?  detail : detail * (i + 1);
            int pointNum = dropletSize * (i + 1);
            int hpointNum = dropletSize * (i + 1) / 10;

            rings[i] = new Ring(radius, i, pointNum, hpointNum);
        }
        for (int i = rings.length - 1; i >= 0; i--) {
            for (int j = 0; j < rings[i].points.length; j++) {
                if (i != 0) {
                    rings[i].points[j].oneDeeper = rings[i].points[j].findNearestOneDeeper(i);
                }
            }
        }
        for (int i = 0; i < rings.length; i++) {
            rings[i].update();
        }
    }
    
    class Ring {
        int index, expandTick;
        Point[] points;
        HighlightPoint[] hpoints;
        
        // allow HighlightPoints to access the same base fade that each ring has
        // (they will be doing some additional fading on top of that as well)
        float baseFade;
        
        // 0 index Ring has a boost in detail
        Ring(int radius, int index, int pointNum, int hpointNum) {
            this.index = index;
            expandTick = index;

            points = new Point[pointNum];
            for (int i = 0; i < points.length; i++) {
                float angle = TWO_PI * i / points.length;
                EPVector pos = new EPVector(radius, 0, 0);
                pos.rotateY(angle);
                points[i] = new Point(pos, index);
            }

            hpoints = new HighlightPoint[hpointNum];
            for (int i = 0; i < hpoints.length; i++) {
                float angle = random(0, TWO_PI);
                EPVector pos = new EPVector(radius, 0, 0);
                pos.rotateY(angle);
                float size = random(1, 3);
                float speed = random(0.8f, 1.1f);
                hpoints[i] = new HighlightPoint(pos, speed, size);
            }
        }

        //converts alpha value to a ratio and multplies every color by that ratio (lets us use blend modes)
        public void setColor(float[] colors) {
            float fade = max(colors[3], 30) / 255.0f;
            fade += currExpand;
            fade = min(fade, 1);

            // slightly fades the outer edges of the plane
            fade *= pow((SPEC_SIZE - index) * 1.0f / SPEC_SIZE, 5.0f / 6.0f);

            // set baseFade so that the HighlightPoints can access this fading when they have to set their
            // color
            baseFade = fade;
            
            stroke(colors[0] * fade, colors[1] * fade, colors[2] * fade); 
        }
        
        public void update() {
            expandTick--;
            // expandTick %= SPEC_SIZE;
            for (int i = 0; i < points.length; i++) {
                points[i].update(index, expandTick);
                points[i].botColors = getColor(-points[i].naturalY, PEAK, colorTrackers[0], colorTrackers[1]);
                points[i].topColors = getColor(-points[i].naturalY, PEAK, colorTrackers[2], colorTrackers[3]);
            }

            float incomingSignal = getIntensity(index) / 2;
            // float incomingSignal = getGreatestMag(SPEC_SIZE) / 3;
            for (HighlightPoint hp : hpoints) {
                hp.update(incomingSignal);
            }
        }
        
        // ydir is -1 or 1: determines whether the figure is draw top up or top down
        public void drawRing(int ydir) {
            noFill();

            float strokeFactor = (expand) ? 4 : 2;
            float currWeight = 1 + ((float) index) / SPEC_SIZE * strokeFactor;
            strokeWeight(currWeight);
            // strokeWeight(1.5);

            if (!particles) {
                beginShape(LINES);
            }

            for (int i = 0; i < points.length; i++) {
                Point curr = points[i % points.length];
                Point next = points[(i + 1) % points.length]; // last index -> zero index
                if (ydir > 0) {
                    setColor(curr.botColors);
                } else {
                    setColor(curr.topColors);
                }

                if (particles) {
                    drawParticle(curr, ydir);
                    drawParticle(next, ydir);
                } else {
                    vertex(curr.pos.x, curr.pos.y * ydir, curr.pos.z);
                    vertex(next.pos.x, next.pos.y * ydir, next.pos.z);
                }

                Point oneDeeper = points[i % points.length].oneDeeper;
                if (this.index != 0) {
                    if (particles) {
                        drawParticle(curr, ydir);
                    } else {
                        vertex(curr.pos.x, curr.pos.y * ydir, curr.pos.z);
                    }
                    if (ydir > 0) {
                        setColor(oneDeeper.botColors);
                    } else {
                        setColor(oneDeeper.topColors);
                    }
                    if (particles) {
                        drawParticle(oneDeeper, ydir);
                    } else {
                        vertex(oneDeeper.pos.x, oneDeeper.pos.y * ydir, oneDeeper.pos.z);
                    }
                }
            }
            
            // if auto rotating, then draws an extra smaller ring before rotating again
            // (this makes sure that we don't have unconnected lines showing)
            if (this.index != 0) {
                for (int i = 0; i < rings[index - 1].points.length + 1; i++) {
                    Point curr = rings[index - 1].points[i % rings[index - 1].points.length];

                    // last index -> zero index
                    Point next = rings[index - 1].points[(i + 1) % rings[index - 1].points.length];
                    
                    if (ydir > 0) {
                        setColor(curr.botColors);
                    } else {
                        setColor(curr.topColors);
                    }
                    if (particles) {
                        drawParticle(curr, ydir);
                        drawParticle(next, ydir);
                    } else {
                        vertex(curr.pos.x, curr.pos.y * ydir, curr.pos.z);
                        vertex(next.pos.x, next.pos.y * ydir, next.pos.z);
                    } 
                }
            }

            if (!particles) {
                endShape();
            }

            float baseY = points[0].pos.y;
            float[] c = (ydir > 0) ? points[0].botColors : points[0].topColors;
            for (HighlightPoint hp : hpoints) {
                hp.drawHighlightPoint(baseY, ydir, c, baseFade);
            }
        }

        public void drawParticle(Point p, int ydir) {
            float weight = abs(p.naturalY) + abs(p.pos.y) * currExpand * 0.25f;
            float w2 = bindRange(weight * PART_SCALE, MIN_PART_SIZE, MAX_PART_SIZE);
            spriteShader.set("weight", w2);
            strokeWeight(w2);
            point(p.pos.x, p.pos.y * ydir, p.pos.z);
        }
    }
    
    class Point {
        EPVector pos;

        // always use point.expandedY , the expandedY will
        // store the natural y position of the point + whatever expansion amt we need.
        // obviously the expansion amt is zero when not expanding, so during those times
        // expandedY will just hold the natural y position
        float naturalY;

        // we are re-using the same samples to draw both bottom and top - but bottom and top need
        // different NON-COMPLEMENTARY colors. so each point keeps track of the two set of colors
        // it will display as
        float[] botColors;
        float[] topColors;

        Point oneDeeper;
        int index;
 
        Point(EPVector pos, int index) {
            this.pos = pos;
            naturalY = pos.y;
            this.index = index;
            oneDeeper = null; 
            botColors = new float[4];
            topColors = new float[4];   
        }
        
        public void update(int index, int expandTick) {
            if (naturalY < 0) {
                naturalY += DECAY + abs(naturalY / 20);
                naturalY = min(0, naturalY);
            }
            float incomingSignal = -1.5f * getIntensity(index);
            if (naturalY > incomingSignal) {
                naturalY = incomingSignal;    
            }
            pos.y = getExpandedY(expandTick);
        }
        
        // finds the equivalent Point to this Point that is located on a ring
        // one deeper than this Point's current ring
        // ringIndex must not equal zero
        public Point findNearestOneDeeper(int ringIndex) {
            int nearestIndex = 0;
            float closestDist = PVector.dist(pos, rings[ringIndex - 1].points[nearestIndex].pos);
            for (int i = 1; i < rings[ringIndex - 1].points.length; i++) {
                float currentDist = PVector.dist(pos, rings[ringIndex - 1].points[i].pos);
                if (currentDist < closestDist) {
                    nearestIndex = i;
                    closestDist = currentDist;
                }
            }
            return rings[ringIndex - 1].points[nearestIndex];
        }

        public float getExpandedY(int expandTick) {
            if (currExpand > 0) {
                // expandTick is decremented in update. keeps the sin wave moving forward.
                // "- currExpand * amp" shifts the planes vertically apart so the waves don't 
                // overlap
                float time = TWO_PI * expandTick / SPEC_SIZE * 1.3f;
                float amp = 40 * sqrt(index * 1.0f / SPEC_SIZE);
                return naturalY - currExpand * amp * sin(time) - currExpand * amp;
            } else {
                return naturalY;
            }
        }
    }

    class HighlightPoint {
        float speed, size;
        EPVector pos;
        boolean continueHighlighting;

        HighlightPoint(EPVector pos, float speed, float size) {
            this.speed = speed;
            this.size = size;
            this.pos = pos;
        }

        public void update(float intensity) {
            if (continueHighlighting) {
                pos.y -= intensity;
                pos.y -= speed;
            }
            if (abs(pos.y) >= HIGHLIGHT_POINT_STOP) {
                if (!highlight) {
                    continueHighlighting = false;
                }
                pos.y = 0;
                float angle = random(0, TWO_PI);
                pos.rotateY(angle);
            }
        }

        public void drawHighlightPoint(float baseY, float ydir, float[] colors, float baseFade) {
            if (continueHighlighting) {
                float fade = 1 - abs(pos.y) / HIGHLIGHT_POINT_STOP;
                fade *= baseFade;
                stroke((255 - colors[0]) * fade, (255 - colors[1]) * fade, (255 - colors[2]) * fade);
                strokeWeight(size * 4);
                point(pos.x, (baseY + pos.y) * ydir, pos.z);
            }
        }
    }
    
    public @Override
    void draw() {
        if (blur) {
            setBackground(contrast, 50);
        } else {
            setBackground(contrast, 150);
        }

        hint(DISABLE_DEPTH_MASK);

        if (expand && currExpand < 1) {
            currExpand += EXPAND_RATE;
        } else if (!expand && currExpand > 0) {
            currExpand -= EXPAND_RATE;    
        }

        if (!expand && currExpand < 0) {
            currExpand = 0;
        }

        if (expand && currExpand > 1) {
            currExpand = 1;
        }

        pushMatrix();
        

        camera.update();
        
        if (!pause) {
            for (int i = 0; i < rings.length; i++) {
                rings[i].update();
            }
            for (ColorTracker ct : colorTrackers) {
                ct.incrementColor();
            }
        }
        if (followMouse) {
            dropletXRot = lerp(dropletXRot, map(mouseY/2, 0, height/2, -PI, PI), .05f);
            dropletYRot = lerp(dropletYRot, map(mouseX/2, 0, width/2, -PI, PI), .05f);
        } else {
            dropletXRot = lerp(dropletXRot, 0, .05f);
            dropletYRot = lerp(dropletYRot, 0, .05f);
            rotater.update();
        }
        rotateX(-dropletYRot);
        rotateZ(-dropletXRot);         
        // if the camera is above the figure, the bottom rings are drawn last. If the camera is below the figure,
        // the top rings are drawn last.
        if (camera.pos.y > 0) { 
            drawInOrder(1, -1);
        } else {
            
            drawInOrder(-1, 1);
        } 

        popMatrix();
    }
    
    public void drawInOrder(int front, int behind) {
        int mult;
        int order;
        for (int i = (rings.length - 1) * 2; i >= 0; i--) {
            if (i > rings.length - 1) {
                order = front;    
            } else {
                order = behind;    
            }

            // the first 5 rings are rotated together
            if (i % (rings.length - 1) > 5) {
                mult = i;
            } else {
                mult = 5;
            }           
//            rotateZ(PI/2);
            rotateX(rotater.xRot * mult);
            rotateY(rotater.yRot * mult);
            rings[i % (rings.length - 1)].drawRing(order);
            rotateY(-rotater.yRot * mult);
            rotateX(-rotater.xRot * mult);
        }
    }

    public @Override
    void particles() {
        particles = !particles;
        if (particles) {
            particleDetail = 1;
            // if (particleDetail != -1) {
            //     dropletSize = particleDetail;
            // }
            // dropletSize = dropletSize >= 2 ? dropletSize -1: dropletSize;
        } else {
            // dropletSize++;
            dropletSize = MAX_DROPLET_SIZE;
        }
        dropletSize = dropletSize >= 1 ? dropletSize : 1;
        setupDroplet();
        if (highlight) {
            for (Ring r : rings) {
                for (HighlightPoint hp : r.hpoints) {
                    hp.continueHighlighting = true;
                }
            }
        }
        blur = particles;
    }

    public @Override
    void highlight() {
        for (Ring r : rings) {
            for (HighlightPoint hp : r.hpoints) {
                hp.continueHighlighting = true;
            }
        }
        highlight = !highlight;
    }

    public @Override
    void expand() {
        expand = !expand;
    }

    public @Override
    void revolve() { 
        revolve = !revolve;
        rotater.autoSwitch();
        if (!revolve) {
            rotater.initRotate(0, 0, (int) frameRate * 10);    
        }
    }
    
    public @Override
    void frontView() {
        // camera.initMoveCamera(new PVector(0, 0, 400), (int) frameRate * 2);
        camera.initMoveCamera(new PVector(-350, 0, .0001f), (int) frameRate * 2);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate * 2);
    }
    
    public @Override
    void rearView() {
        camera.initMoveCamera(new PVector(10, 180, 0.001f), (int) frameRate * 2);
        // camera.initMoveCamera(new PVector(400, -300, 0), (int) frameRate * 2);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate * 2);
    }
    
    public @Override
    void topView() { 
        camera.initMoveCamera(new PVector(.001f, -400, 0), (int) frameRate * 2);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate * 2);
    }

    public @Override
    void pause() {
        pause = !pause;
    }
 
    public @Override
    void adjustDetail(float avgFr) {
        // println(avgFr);
        if (avgFr < 25) {
            particleDetail = 1;
        } else if (avgFr < 28) {
            particleDetail = MAX_DROPLET_SIZE - 2;
        } else if (avgFr < 32) {
            particleDetail = MAX_DROPLET_SIZE - 1;
        } else if (avgFr < 35) {
           particleDetail = MAX_DROPLET_SIZE;
        }
        if(particleDetail == -1){
            dropletSize = MAX_DROPLET_SIZE;
        } else {
            dropletSize = particleDetail;
        }
        setupDroplet();
    }

    public @Override
    void autoPan() {
    }

    public @Override
    void keyPressed() {
        super.keyPressed();
        switch (keyCode) {
             // case 38:
             //     dropletSize++;;
             //     setupDroplet();
             //     break;
             // case 40:
             //     if (dropletSize > 1) {
             //         dropletSize--;
             //         setupDroplet();
             //     }
             //     break;
            default:
                break;
        }
    }
}
class EPVector extends PVector {
    PVector temp;
    EPVector(float x, float y, float z) {
        super(x, y, z);  
        temp = new PVector();     
    }
    
    EPVector() {
        super(0, 0, 0);   
        temp = new PVector();    
    }
    
    public void rotateX(float angle) {
        temp.x = super.y;
        temp.y = super.z;
        temp.rotate(angle);
        super.y = temp.x;
        super.z = temp.y;
    }
    
    public void rotateY(float angle) {
        temp.x = super.x;
        temp.y = super.z;
        temp.rotate(angle);
        super.x = temp.x;
        super.z = temp.y;
    }
    
    public void rotateZ(float angle) {
        temp.x = super.x;
        temp.y = super.y;
        temp.rotate(angle);
        super.x = temp.x;
        super.y = temp.y;
    }
    
    public void set(int x, int y, int z){
        super.x = x;
        super.y = y;
        super.z = z;
    }
}
 

class Fluid extends Visualizer {
    public @Override
    int getOptimalFrameRate() {
        return 40;
    }

    final int SPEC_SIZE = 30;
    final float SPEC_WIDTH = 5;
    final int HORIZ_SAMPLE_NUM = 80;
    final int VERT_SAMPLE_NUM = 30;
    final int REFRESH = 3;
    final float ANGLE_INC = 0.001f;
    final float MIN_PARTICLE_SIZE = 2;
    final float MAX_PARTICLE_SIZE = 20;
    

    // since we need 4 different color trackers -- base and peak colors for both
    // bottom and top halves -- stored all dem in an array
    // colorTrackers[0] -> base tracker for bottom half
    // colorTrackers[1] -> peak tracker for bottom half
    // colorTrackers[2] -> base tracker for top half
    // colorTrackers[3] -> peak tracker for top half
    ColorTracker[] colorTrackers;
    
    HorizSample[] horizSamples;
    VertSample[] vertSamples;
    float fluidXRot, fluidYRot;
    
    float currRot = 0;

    int particleDetailLoss = 1;
    
    Fluid(AudioInput input) {
        super(input, "TERRAIN");
        colorTrackers = new ColorTracker[4];
        for (int i = 0; i < colorTrackers.length; i++) {
            colorTrackers[i] = new ColorTracker(0.5f, 4);   
        }
        camera.setCenter(SPEC_SIZE * SPEC_WIDTH, 0, 0);
        horizSamples = new HorizSample[HORIZ_SAMPLE_NUM];
        vertSamples = new VertSample[VERT_SAMPLE_NUM];
        for (int i = 0; i < horizSamples.length; i++) {
            horizSamples[i] = new HorizSample(i * REFRESH, REFRESH, HORIZ_SAMPLE_NUM * REFRESH);
        }
        for (int i = 0; i < vertSamples.length; i++) {
            vertSamples[i] = new VertSample(i * REFRESH, REFRESH, VERT_SAMPLE_NUM * REFRESH);
        }
        camera.viewingMode = false;
        camera.pos = new PVector(SPEC_SIZE * SPEC_WIDTH, 0, -130);
        camera.setOuterBounds(0, -200, -200, SPEC_SIZE * SPEC_WIDTH * 2, 200, REFRESH * HORIZ_SAMPLE_NUM);
        // noFill();
    }

    class Point {
        float x, y, z, intensity;

        // we are re-using the same samples to draw both bottom and top - but bottom and top need
        // different NON-COMPLEMENTARY colors. so each point keeps track of the two set of colors
        // it will display as
        float[] topColors;
        float[] botColors;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            topColors = new float[4];
            botColors = new float[4];
        }
    }

    class HorizSample {
        float pos, speed, stop;
        int index;
        Point[] points;

        HorizSample(float initPos, float speed, float stop) {
            this.speed = speed;
            this.stop = stop;
            index = (int) (initPos / speed);
            pos = initPos;
            points = new Point[SPEC_SIZE * 2];
            for (int i = 0; i < points.length; i++) {
                points[i] = new Point(i * SPEC_WIDTH, 0, 0);
            }
        }
        
        public void setColor(float fade, float[] colors) {
            stroke(colors[0] * fade, colors[1] * fade, colors[2] * fade);
            // fill(colors[0] * fade*.1, colors[1] * fade*.1, colors[2] * fade*.1);
        }        

        public void update() {

            pos += speed;  
            if (expand) {
                for (int i = 0; i < points.length; i++) {
                    points[i].y += pos / 40;
                }
            }
            if (pos >= stop) {
                for (int i = 0; i < points.length; i++) {
                    int fftIndex = (int)round(abs(points.length / 2.0f - i));
                    points[i].y = getIntensity(fftIndex);
                    points[i].intensity = getIntensity(fftIndex);

                    // see comment inside Point (above botColors and topColors)
                    // for explanation on wtf is going on here
                    points[i].botColors = getColor(points[i].intensity, 40, colorTrackers[0], colorTrackers[1]);
                    points[i].topColors = getColor(points[i].intensity, 40, colorTrackers[2], colorTrackers[3]);
                }
                pos = 0;
            }
        }

        public void drawLines(int ydir, float fade) {
            pushMatrix();

            if (pos > 0) {

                HorizSample currSample = this;
                int prevIndex;
                if (index == 0) {
                    prevIndex = horizSamples.length - 1;
                } else {
                    prevIndex = index - 1;
                }

                HorizSample prevSample = horizSamples[prevIndex];

                // strokeWeight cannot being changed while inside beginShape/endShape,
                // so we must use point() instead of vertex() when drawing particles
                if (!particles) {
                    beginShape(QUAD_STRIP);
                }

                float zEnd = prevSample.pos;
                float zStart = currSample.pos;
                float tempFade = fade;
                for (int i = 0; i < points.length; i++) {
                    float xStart = currSample.points[i].x;
                    float xEnd = prevSample.points[i].x;
                    float yStart = currSample.points[i].y * ydir;
                    float yEnd = prevSample.points[i].y * ydir;
                    if(!expand) { 
                        if (abs(yEnd - yStart) <= 1)
                            tempFade = 0.1f;
                        else
                            tempFade = fade * abs(1-(yEnd / volumeScale / (PHI-1) - yStart / volumeScale / (PHI-1))/5.0f);
                    }
                    if (ydir > 0) {
                        setColor(tempFade, points[i].botColors);
                    } else {
                        setColor(tempFade, points[i].topColors);
                    }

                    if (!particles) {
                        vertex(xStart, yStart, zStart);
                        vertex(xEnd, yEnd, zEnd);
                    } else if (i % particleDetailLoss == 0) {
                        if(!expand) {
                            strokeWeight(bindRange(currSample.points[i].intensity, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                        }
                        spriteShader.set("weight", bindRange(currSample.points[i].intensity, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                        point(xStart, yStart, zStart);

                        strokeWeight(bindRange(prevSample.points[i].intensity, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                        point(xEnd, yEnd, zEnd);
                    // } else if (i % particleDetailLoss == 0) {
                    //     strokeWeight(bindRange(currSample.points[i].intensity, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                    //     point(xStart, yStart, zStart);
                    }
                }  

                if (!particles) {
                    endShape();
                }
            } 
            popMatrix();
        }
    }

    class VertSample {
        float pos, stop, speed;
        PVector[] points;
        boolean continueSampling;

        VertSample(float initPos, float speed, float stop) {
            pos = initPos;
            this.speed = speed;
            this.stop = stop;
            points = new PVector[SPEC_SIZE * 2];
            for (int i = 0; i < points.length; i++) {
                points[i] = new PVector(i * SPEC_WIDTH, 0);
            }
            continueSampling = false;
        }

        public void update() {
            pos += speed;
            if (pos >= stop) {
                for (int i = 0; i < points.length; i++) {
                    int fftIndex = abs(points.length / 2 - i);
                    points[i].y = getIntensity(fftIndex);
                }
                pos = 0;
                if (highlight) {
                    continueSampling = true;
                } else {
                    continueSampling = false;
                }
            }
        }

        public void drawLines(int ydir) {
            pushMatrix();

            translate(0, pos * ydir, 0);

            if (!particles) {
                beginShape(LINES);
            }

            for (int i = 0; i < points.length - 1; i++) {
                float weight = (!particles)
                    ? bindRange((points[i].y + points[i + 1].y) / 20, 1, 6)
                    : bindRange(points[i].y / 2, 1, MAX_PARTICLE_SIZE);

                strokeWeight(weight);
                if (!particles) {
                    vertex(points[i].x, points[i].y * ydir);
                    vertex(points[i + 1].x, points[i + 1].y * ydir);
                } else if (i % particleDetailLoss == 0) {
                    strokeWeight(bindRange(weight, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                    point(points[i].x, points[i].y * ydir);
                }
            }

            float weight = min((points[points.length - 2].y + points[points.length - 1].y) / 20, 6);
            strokeWeight(weight);
            if (!particles) {
                vertex(points[points.length - 2].x, points[points.length - 2].y * ydir);
                vertex(points[points.length - 1].x, points[points.length - 1].y * ydir);
            } else {
                strokeWeight(bindRange(weight, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE));
                point(points[points.length - 2].x, points[points.length - 2].y * ydir);
            }

            if (!particles) {
                endShape();
            }

            popMatrix();
        }
    }

    public @Override
    void draw() {
        if (blur) {
            setBackground(contrast, 80);
        } else {
            setBackground(contrast, 255);
            // setBackground(contrast, 150);
        }

        hint(DISABLE_DEPTH_MASK);
        camera.update();
        // --------------------------------------------------- Rotate Fluid
        if(revolve) {
            translate(0, 0, HORIZ_SAMPLE_NUM * REFRESH/2);
        } else {
            translate(SPEC_SIZE*SPEC_WIDTH, 0, HORIZ_SAMPLE_NUM * REFRESH/2);
        }
        if (followMouse) {
            fluidXRot = lerp(fluidXRot, map(mouseY/2, 0, height/2, -PI, PI), .05f);
            fluidYRot = lerp(fluidYRot, map(mouseX/2, 0, width/2, -PI, PI), .05f);
        } else {
            fluidXRot = lerp(fluidXRot, 0, .05f);
            fluidYRot = lerp(fluidYRot, 0, .05f);
        }
        rotateX(fluidXRot);
        rotateY(fluidYRot);
        if(revolve) {
            translate(0, 0, -HORIZ_SAMPLE_NUM * REFRESH/2);
        } else {
            translate(-SPEC_SIZE*SPEC_WIDTH, 0, -HORIZ_SAMPLE_NUM * REFRESH/2);
        }
        noFill();
        pushMatrix();
    
        // makes sure vertical samples appear at the front of the figure
        if (revolve) {
            translate(0, 0, 170);
        }
        if (!pause) {
            for (ColorTracker ct : colorTrackers) {
                ct.incrementColor();
            }

            if (revolve) {
                currRot += ANGLE_INC;
            } else {
                if(currRot > 0){
                    currRot -= ANGLE_INC;
                    currRot = max(0, currRot);
                }
            }

            for (int i = 0; i < VERT_SAMPLE_NUM; i++) {
                vertSamples[i].update();
            }
        }
        for (int i = 0; i < VERT_SAMPLE_NUM; i++) {
            VertSample s = vertSamples[i];
            if (s.continueSampling) {
                rotateZ(currRot);
                float fade = 1 - s.pos / (VERT_SAMPLE_NUM * REFRESH);
                setComplementaryColor(fade, colorTrackers[0]);
                s.drawLines(1);
                setComplementaryColor(fade, colorTrackers[2]);
                s.drawLines(-1);
            }
        } 

        popMatrix();

        pushMatrix();

        strokeWeight(1);
        if (!pause){
            for (int i = 0; i < HORIZ_SAMPLE_NUM; i++) {
                horizSamples[i].update();
            }
        }
        for (int i = 0; i < HORIZ_SAMPLE_NUM; i++) {
            HorizSample s = horizSamples[i];
            int relativeIndex = (int) (s.pos / REFRESH);
            rotateZ(currRot * relativeIndex);

                
            if (expand) {
                float weight = map(s.pos, 0, s.stop, 0.8f, 5);
                strokeWeight(weight);
            }
            
            
            float fade;
            if (expand) {
                fade = 1 - s.pos / (HORIZ_SAMPLE_NUM * REFRESH) / 2;
            } else {
                fade = min(1 - s.pos / (HORIZ_SAMPLE_NUM * REFRESH), .3f);
                // if(1-s.pos == 1 || s.pos < 5) //sets only the front to full color
                //     fade = 1;
            }
            
            // for (int j = 0; j < s.points.length; j++) {
            //     if(s.points[j].y >= mag)
            //         fade = 1;
            // }
            s.drawLines(1, fade);
            s.drawLines(-1, fade);  
            rotateZ(-currRot * relativeIndex);
            
        }
        
        popMatrix();
    }
    
    public void setComplementaryColor(float fade, ColorTracker tracker) {
        stroke((255 - tracker.red) * fade, (255 - tracker.green) * fade, (255 - tracker.blue) * fade);
    }

    public @Override
    void adjustDetail(float avgFr) {
        if (avgFr < 25) {
            particleDetailLoss = 5;
        } else if (avgFr < 30) {
            particleDetailLoss = 4;
        } else if (avgFr < 35) {
            particleDetailLoss = 3;
        } else if (avgFr < 38) {
            particleDetailLoss = 2;
        }
        // println(particleDetailLoss);
    }

    public @Override
    void particles() {
        particles = !particles;
        blur = particles;
    }

    public @Override
    void highlight() {
        highlight = !highlight;
    }

    public @Override
    void expand() {
        expand = !expand;
    }

    public @Override
    void revolve() { 
        revolve = !revolve;
        if (!revolve && currRot >= .082f) {
            currRot = .082f; //sets revolve to 1 full rotation
        }
        if(revolve) {
            camera.setOuterBounds(-SPEC_SIZE * SPEC_WIDTH, -200, -200, SPEC_SIZE * SPEC_WIDTH, 200, REFRESH * HORIZ_SAMPLE_NUM);
        } else {
            camera.setOuterBounds(0, -200, -200, SPEC_SIZE * SPEC_WIDTH * 2, 200, REFRESH * HORIZ_SAMPLE_NUM);
        }
        fPressed();
        frontView();
    }

    public @Override
    void pause() {
        pause = !pause;
    }

    public @Override
    void frontView() {
        float camX = SPEC_SIZE * SPEC_WIDTH;
        if (revolve) {
            camera.initMoveCenter(0, 0, 0, (int)frameRate);
            camX = 0;
        } else {
            camera.initMoveCenter(SPEC_SIZE * SPEC_WIDTH, 0, 0, (int)frameRate);
        }
        camera.initMoveCamera(new PVector(camX, 0, -130), (int)frameRate);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate);
    }
    
    public @Override
    void rearView() {
        float camX = SPEC_SIZE * SPEC_WIDTH;
        if (revolve) {
            camera.initMoveCenter(0, 0, 0, (int)frameRate);
            camX = 0;
        }
        camera.initMoveCamera(new PVector(camX, 0, 300), (int)frameRate);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate);
    }
    
    
    public @Override
    void topView() { 
        float camZ = HORIZ_SAMPLE_NUM * REFRESH/ 1.99f;
        float camY = -150;
        if (frontView) {
            camZ = HORIZ_SAMPLE_NUM * REFRESH / 2.1f;
            camY = 160;
        }
        
        if (revolve) {
            camera.initMoveCamera(new PVector(-150, camY, camZ), (int) frameRate * 2);
            camera.initMoveCenter(0, 0, HORIZ_SAMPLE_NUM * REFRESH / 2, (int) frameRate / 2);
        } else {
            camera.initMoveCamera(new PVector(150, camY, camZ), (int) frameRate * 2);
            camera.initMoveCenter(SPEC_SIZE * SPEC_WIDTH, 0, HORIZ_SAMPLE_NUM * REFRESH / 2, (int) frameRate);
        }
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate);
    }

    public @Override
    void autoPan() {
        float camZ = HORIZ_SAMPLE_NUM * REFRESH/ 1.99f;
        float camY = -150;
        if (frontView) {
            camZ = HORIZ_SAMPLE_NUM * REFRESH / 2.1f;
            camY = 160;
        }
        if (revolve) {
            camera.initMoveCenter(0, 0, HORIZ_SAMPLE_NUM * REFRESH / 2, (int) frameRate / 2);
        } else {
            camera.initMoveCenter(SPEC_SIZE * SPEC_WIDTH, 0, HORIZ_SAMPLE_NUM * REFRESH / 2, (int) frameRate);
        }
    }

}
class Particle {
  PVector pos = new PVector(0, 0);
  PVector vel = new PVector(0, 0);
  PVector acc = new PVector(0, 0);
  PVector target = new PVector(0, 0);

  float closeEnoughTarget = 50;
  float maxSpeed = 4.0f;
  float maxForce = 0.1f;
  float particleSize = 5;
  boolean isKilled = false;

  int startColor = color(0);
  int targetColor = color(0);
  float colorWeight = 0;
  float colorBlendRate = 0.025f;

  public void move() {
    // Check if particle is close enough to its target to slow down
    float proximityMult = 1.0f;
    float distance = dist(this.pos.x, this.pos.y, this.target.x, this.target.y);
    if (distance < this.closeEnoughTarget) {
      proximityMult = distance/this.closeEnoughTarget;
    }

    // Add force towards target
    PVector towardsTarget = new PVector(this.target.x, this.target.y);
    towardsTarget.sub(this.pos);
    towardsTarget.normalize();
    towardsTarget.mult(this.maxSpeed*proximityMult);

    PVector steer = new PVector(towardsTarget.x, towardsTarget.y);
    steer.sub(this.vel);
    steer.normalize();
    steer.mult(this.maxForce);
    this.acc.add(steer);

    // Move particle
    this.vel.add(this.acc);
    this.pos.add(this.vel);
    this.acc.mult(0);
  }

  public void draw() {
    // Draw particle
    int currentColor = lerpColor(this.startColor, this.targetColor, this.colorWeight);
    if (drawAsPoints) {
      stroke(currentColor);
      point(this.pos.x, this.pos.y);
    } else {
      noStroke();
      fill(currentColor);
      //ellipse(this.pos.x, this.pos.y, this.particleSize, this.particleSize);
      rect(this.pos.x, this.pos.y, this.particleSize, this.particleSize);
    }

    // Blend towards its target color
    if (this.colorWeight < 1.0f) {
      this.colorWeight = min(this.colorWeight+this.colorBlendRate, 1.0f);
    }
  }

  public void kill() {
    if (! this.isKilled) {
      // Set its target outside the scene
      PVector randomPos = generateRandomPos(width/2, height/2, (width+height)/2);
      this.target.x = randomPos.x;
      this.target.y = randomPos.y;

      // Begin blending its color to black
      this.startColor = lerpColor(this.startColor, this.targetColor, this.colorWeight);
      this.targetColor = color(0);
      this.colorWeight = 0;

      this.isKilled = true;
    }
  }
}


// Picks a random position from a point's radius
public PVector generateRandomPos(int x, int y, float mag) {
  PVector randomDir = new PVector(random(0, width), random(0, height));
  
  PVector pos = new PVector(x, y);
  pos.sub(randomDir);
  pos.normalize();
  pos.mult(mag);
  pos.add(x, y);
  
  return pos;
}


// Makes all particles draw the next word
public void nextWord(String word) {
  // Draw word in memory
  PGraphics pg = createGraphics(width, height);
  pg.beginDraw();
  pg.fill(0);
  pg.textSize(100);
  pg.textAlign(CENTER);
  PFont font = createFont(fontName, 100);
  pg.textFont(font);
  pg.text(word, width/2, height/2);
  pg.endDraw();
  pg.loadPixels();

  // Next color for all pixels to change to
  int newColor = color(random(0.0f, 255.0f), random(0.0f, 255.0f), random(0.0f, 255.0f));

  int particleCount = particlesme.size();
  int particleIndex = 0;

  // Collect coordinates as indexes into an array
  // This is so we can randomly pick them to get a more fluid motion
  ArrayList<Integer> coordsIndexes = new ArrayList<Integer>();
  for (int i = 0; i < ((width/2)*(height/2)-1); i+= pixelSteps) {
    coordsIndexes.add(i);
  }

  for (int i = 0; i < coordsIndexes.size (); i++) {
    // Pick a random coordinate
    int randomIndex = (int)random(0, coordsIndexes.size());
    int coordIndex = coordsIndexes.get(randomIndex);
    coordsIndexes.remove(randomIndex);
    
    // Only continue if the pixel is not blank
    if (pg.pixels[coordIndex] != 0) {
      // Convert index to its coordinates
      int x = coordIndex % width;
      int y = coordIndex / width;

      Particle newParticle;

      if (particleIndex < particleCount) {
        // Use a particle that's already on the screen 
        newParticle = particlesme.get(particleIndex);
        newParticle.isKilled = false;
        particleIndex += 1;
      } else {
        // Create a new particle
        newParticle = new Particle();
        
        PVector randomPos = generateRandomPos(width/2, height/2, (width+height)/2);
        newParticle.pos.x = randomPos.x;
        newParticle.pos.y = randomPos.y;
        
        newParticle.maxSpeed = random(2.0f, 5.0f);
        newParticle.maxForce = newParticle.maxSpeed*0.025f;
        newParticle.particleSize = random(3, 6);
        newParticle.colorBlendRate = random(0.0025f, 0.03f);
        
        particlesme.add(newParticle);
      }
      
      // Blend it from its current color
      newParticle.startColor = lerpColor(newParticle.startColor, newParticle.targetColor, newParticle.colorWeight);
      newParticle.targetColor = newColor;
      newParticle.colorWeight = 0;
      
      // Assign the particle's new target to seek
      newParticle.target.x = x;
      newParticle.target.y = y;
    }
  }

  // Kill off any left over particles
  if (particleIndex < particleCount) {
    for (int i = particleIndex; i < particleCount; i++) {
      Particle particle = particlesme.get(i);
      particle.kill();
    }
  }

}
 
 

class Ring extends Visualizer {
    public @Override
    int getOptimalFrameRate() {
        return 40;
    }
    
    int SAMPLE_NUM = 180;
    final int SPEC_SIZE = 50;
    float REFRESH = 2;
    final float ROT_SPEED = PI / 2800;
    final float DIST = PHI * 2; //PHI
    final float ADD_DIST = -10; //-10
    final float INIT_DIST = 20; // 10
    final float MAX_TIME = 2000; //in milliseconds
    final float MAX_SPEED = 0.2f;
    final float MIN_PART_SIZE = 2;
    final float MAX_PART_SIZE = 20;

    EPVector rotationVector; //handles rotating the verticies when revolve is turned on
    float xRot;
    float zRot;
    float explodeVal;
    float ringXRot, ringYRot;

    // we will sample the framerate and adjust this as needed when particle mode is
    // initiated
    float particleDetailLoss = 0;
    
    float deltaRotation = PI / 2000;
    
    ColorTracker tracker;
    ColorTracker tracker2;
    Sample[] samples;
    
    float start = 0;
    float stop = 0;
    float averageSpeed = 0;
    boolean throttlingOn = false;
    
    public Ring(AudioInput input) {
        super(input, "VORTEX");
        tracker = new ColorTracker(0.1f, 0.8f);
        tracker2 = new ColorTracker(0.1f, 0.8f);
        camera.viewingMode = false;
        camera.pos = new PVector(0, 0, -800);
        camera.setOuterBounds(-1000, -1000, -1000, 1000, 1000, 1000);
        rotationVector = new EPVector();
        setupRing();
        start = millis();
    }    

    public void setupRing(){
        samples = new Sample[SAMPLE_NUM];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = new Sample(i * REFRESH, SAMPLE_NUM * REFRESH, SPEC_SIZE, i);
        }
    }
    
    // Samples are slices of the sound
    class Sample {
        Point[] points;
        float pos, stop, rot, rotSpeed;
        int index;
        
        Sample(float pos, float stop, int pointNum, int index) {
            this.pos = pos;
            this.stop = stop;
            this.index = index;
            
            points = new Point[pointNum];
            for (int i = 0; i < points.length; i++) {
                float angle = i * (TWO_PI / points.length);
                
                PVector p = new PVector(0, INIT_DIST + DIST * pow((float)Math.E, angle));
                int rotDir;
                if (i % 2 == 0) {
                    rotDir = 1;
                } else {
                    rotDir = -1;
                }
                
                points[i] = new Point(i, p, pow(points.length - i, 1.168f) * ROT_SPEED, rotDir);
            }
        }
        
        public void update() {
            pos += REFRESH;      
            
            boolean isNewPoint = false;
            float greatestMag = 0.0f;
            if (pos >= stop) {
                pos = 0;
                isNewPoint = true;
                if (highlight) {
                    greatestMag = getGreatestMag(100);
                }
            }
            
            for (int i = 0; i < points.length; i++) {
                Point p = points[i];
                p.updateRot();
                p.pos.z = pos;
                if (isNewPoint) {
                    float angle = i * (TWO_PI / points.length);
                    PVector temp2d = new PVector(0, INIT_DIST + DIST * pow((float)Math.E, angle));
                    temp2d.rotate(p.rot);
                    p.pos = new PVector(temp2d.x, temp2d.y, 0);
                    p.updateSnd(greatestMag);
                    if (highlight) {
                        p.strokeWeight = min(0.3f + p.size, 8);
                    } else {
                        p.strokeWeight = min(0.3f + p.size * 3, 30);
                    }
                }
            } 
        }
        
        public void drawSample() {
            if (pos > 0 && pos < stop - REFRESH) {
                int prevIndex;
                if (index == 0) {
                    prevIndex = samples.length - 1;
                } else {
                    prevIndex = index - 1;
                }
                
                Sample prevSample = samples[prevIndex];
                
                if (revolve) {
                    xRot += .000001f;
                    zRot += .00001f;
                } else {
                    xRot = 0;
                    zRot = 0;
                }                    
                
                if (!particles) {
                    beginShape(LINES);
                }
                for (int i = 0; i < points.length; i++) {
                    points[i].drawPoint(pos, stop, prevSample.points[i], index);
                }
                if (!particles) {
                    endShape();
                } 
            } 
        }
    }
    
    class Point {
        int index, rotDir;
        PVector pos;
        float size, rotSpeed, rot, origMag, greatestMag, strokeWeight;
        float[] colors;
        
        Point(int index, PVector pos, float rotSpeed, int rotDir) {
            this.index = index;
            this.pos = pos;
            this.rotSpeed = rotSpeed;
            origMag = INIT_DIST + (new PVector(pos.x, pos.y)).mag();
            this.rotDir = rotDir;
            colors = new float[4];
        }
        
        public void updateRot() {
            rot += rotSpeed * rotDir;
        }
        
        public void updateSnd(float greatestMag) {
            this.greatestMag = greatestMag;
            size = getIntensity(index) * 0.9f;
            colors = getColor(pos.mag(), 200, tracker, tracker2);
        }
        
        public void drawPoint(float zpos, float stop, Point prevPoint, int sampleIndex) {
            PVector prevPos = prevPoint.pos;
            float fade = pow((stop - zpos) / stop, 5.0f / 6.0f);
            stroke(colors[0] * fade, colors[1] * fade, colors[2] * fade);
            float magnitude = zpos * (ADD_DIST / stop);
            if (!pause) {
                if (prevPoint.pos.z == 0) {
                    PVector p = new PVector(pos.x, pos.y);             
                    if (highlight) {
                        float mag = origMag + abs(greatestMag);
                        p.setMag(mag);
                    }
                    pos.x = p.x;
                    pos.y = p.y;    
                } else {
                    pos.setMag(pos.mag() + magnitude);
                }
            }

            strokeWeight(strokeWeight);
            
            float theta = TWO_PI * index / SPEC_SIZE;
            if (expand && !pause) {
                pos.y -= index / 3.0f;
            }
            rotationVector.set(pos.x, pos.y, pos.z);
            rotationVector.rotateX(theta * xRot);
            rotationVector.rotateZ(theta * zRot);
            if (!particles) {
                vertex(rotationVector.x, rotationVector.y, rotationVector.z);
            } else {
                float weight = bindRange(size * 10, MIN_PART_SIZE, MAX_PART_SIZE);
                spriteShader.set("weight",weight);
                strokeWeight(bindRange(size * 10, MIN_PART_SIZE, MAX_PART_SIZE));
                if (particleDetailLoss == 0) {
                    point(rotationVector.x, rotationVector.y, rotationVector.z);
                } else if(sampleIndex % particleDetailLoss == 0) {
                    point(rotationVector.x, rotationVector.y, rotationVector.z);
                }
            }

            rotationVector.set(prevPos.x, prevPos.y, prevPos.z);
            rotationVector.rotateX(theta * xRot);
            rotationVector.rotateZ(theta * zRot);

            if (!particles) {
                vertex(rotationVector.x, rotationVector.y, rotationVector.z);
            } else {
                if (particleDetailLoss == 0) {
                    point(rotationVector.x, rotationVector.y, rotationVector.z);
                } else if(sampleIndex % particleDetailLoss == 0) {
                    point(rotationVector.x, rotationVector.y, rotationVector.z);
                }
            }
        }
    }

    public @Override
    void draw() {
        if (blur) {
            setBackground(contrast, 40);
        } else { 
            setBackground(contrast, 150);
        }

        hint(DISABLE_DEPTH_MASK);

        if (sampleParticleMode) {
            float avgFr = sampleFrameRate();
            if (avgFr > 0) {
                adjustDetail(avgFr);
            }
        }
        // hint(ENABLE_DEPTH_MASK);

        pushMatrix();

        camera.update();
        if (!pause) {
            tracker.incrementColor();
            tracker2.incrementColor();

            if (millis() - start < stop) {
                averageSpeed = incrRot(deltaRotation);
                if (averageSpeed > MAX_SPEED || averageSpeed < -MAX_SPEED) {
                    throttlingOn = true;
                    deltaRotation = -deltaRotation;
                } else if (((averageSpeed < 0.015f && averageSpeed > 0) || (averageSpeed > -0.015f && averageSpeed < 0))
                        && throttlingOn) {
                    throttlingOn = false;   
                }
            } else {
                start = millis();
                stop = random(0, MAX_TIME);
                if (!throttlingOn) {
                    deltaRotation = -deltaRotation;
                }
            }
            
            for (int i = 0; i < samples.length; i++) {
                samples[i].update();
            }
        }
        // hint(DISABLE_DEPTH_MASK);
        if (followMouse) {
            if(mousePressed){
                println(ringXRot + " " + ringYRot);
            }
            ringXRot = lerp(ringXRot, map(mouseY/2, 0, height/2, -PI, PI), .05f);
            ringYRot = lerp(ringYRot, map(mouseX/2, 0, width/2, -PI, PI), .05f);
        } else {

            ringYRot = lerp(ringYRot, 0, .05f);
            if(topView) { //0.28124383 -9.732737E-10
                ringXRot = lerp(ringXRot, PI/4, .05f);
            } else {
                ringXRot = lerp(ringXRot, 0, .05f);
            }
        }
        rotateX(ringXRot);
        rotateY(ringYRot);

        for (int i = 0; i < samples.length; i++) {
            samples[i].drawSample();
        }

        popMatrix();
    }

    // returns avg rotation of all points
    public float incrRot(float increment) {
        float total = 0;
        float count = 0;
        for (Sample sample : samples) {
            for (Point point : sample.points) {
                point.rotSpeed += increment;
                total += point.rotSpeed;   
                count++;     
            }
        }
        return total / count;
    }

    public @Override
    void adjustDetail(float avgFr) {
        if (avgFr < 30) {
            particleDetailLoss = 8;
        } else if (avgFr < 35) {
            particleDetailLoss = 6;
        } else if (avgFr < 37) {
            particleDetailLoss = 3;
        }
    }

    public @Override
    void particles() {
        particles = !particles;
        blur = particles;
    }

    public @Override
    void highlight() {
        highlight = !highlight;
        // if (!highlight) {
        //     REFRESH = 35;
        //     SAMPLE_NUM = height / 4;
        // } else {
        //     REFRESH = 2;
        //     SAMPLE_NUM = 180;
        // }
        // for (int i = 0; i < samples.length; i++) {
        //      samples[i].stop = SAMPLE_NUM * REFRESH;
        //      samples[i].pos *= REFRESH;
        // }
        // setupRing();     
    }
 
    public @Override
    void expand() {
        expand = !expand;

    }
    
    public @Override
    void revolve(){
        revolve = !revolve;
            // camera.initMoveCamera(new PVector(0, 1300, 0), (int)frameRate*2);
        blur = revolve;
        camera.initMoveCenter(0, 0, 0, (int)frameRate *2);
        if (topView) {
            camera.initMoveCamera(new PVector(0, -REFRESH * SAMPLE_NUM - 600, 0), (int)frameRate * 2);
        }
    }
    
    public @Override
    void frontView() {
        camera.initMoveCamera(new PVector(0, 0, -800), (int)frameRate*2);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate);
    }
    
    public @Override
    void rearView() {
        camera.initMoveCamera(new PVector(0, 0, REFRESH * SAMPLE_NUM), (int)frameRate*2);
        camera.initMoveDir(new PVector(0, 1, 0), (int) frameRate);
        camera.initMoveCenter(0, 0, 0, (int)frameRate *2);
    }
    
    public @Override
    void topView() {
        if(revolve) {
            camera.initMoveCamera(new PVector(0, 1300, 0), (int)frameRate*2);
            camera.initMoveCenter(0, 0, 0, (int)frameRate *2);
        } else {
            camera.initMoveCenter(0, 0, (REFRESH * SAMPLE_NUM)/2, (int)frameRate*2);
            camera.initMoveCamera(new PVector(0, -300 , -1 ), (int)frameRate*2);
            // camera.initMoveDir(new PVector(0, -1, 0), (int) frameRate);
        }
    }

    public @Override
    void autoPan(){
        if(revolve) {
            camera.initMoveCenter(0, 0, 0, (int)frameRate *2);
        } else {
            camera.initMoveCenter(0, 0, (REFRESH * SAMPLE_NUM)/2, (int)frameRate*2);
        }
    }

    public @Override
    void pause() {
        pause = !pause;
    }
    
    public void leftView(){
        camera.initMoveCamera(new PVector(-176, -121, 0), (int)frameRate*2);
        camera.initMoveCenter(0, 0, width/4, (int)frameRate);
    }
    
    public @Override
    void keyPressed() {
        super.keyPressed();
        if(key == 'l')
            leftView();
    }

}
class RotationTracker {
    float xRot, yRot, zRot, xStart, yStart,zStart, xEnd, yEnd, zEnd;
    boolean manualRotate, autoRotate, rotating;
    int currentTime, moveTime;
    
    RotationTracker() {
        xRot = 0;
        yRot = 0;
        zRot = 0;
        manualRotate = false;
        autoRotate = false;
        rotating = false;    
    }
    
    public void autoSwitch() {
        autoRotate = !autoRotate;
        manualRotate = false; 
    }
    
    public void manualSwitch() {
        manualRotate = !manualRotate;
        autoRotate = false;
        rotating = false;  
    }
    
    public void initRotate(float xDestination, float yDestination, int time) {
        initRotate(xDestination, xDestination, 0, time);
    }
    
    public void initRotate(float xDestination, float yDestination, float zDestination, int time) {
        xStart = xRot;
        yStart = yRot;
        zStart = zRot;
        xEnd = xDestination;
        yEnd = yDestination;
        zEnd = zDestination;
        moveTime = time; 
        currentTime = 0;
        rotating = true; 
    }
    
    public void update() {
        if (manualRotate) {
            xRot = map(mouseX, 0, width, 0, PI);    
            yRot = map(mouseY, 0, height, 0, PI); 
        }   
        
        if (autoRotate && !rotating) {
            float x = random(0, PI);
            float y = random(0, PI);
            float z = random(0, PI);
            float dist = sqrt(sq(x) + sq(y)+sq(z));
            int baseLine = (int) random(5 * frameRate, 10 * frameRate);
            int time = baseLine + (int)(75 * frameRate * (dist / PI));
            initRotate(x, y, z, time);
        }
        
        if (rotating) {
            float angle = (currentTime*1.0f / moveTime) * PI;

            float xAmp = ((xEnd - xStart) * PI) / (2 * moveTime);
            float dx = xAmp * sin(angle);
            xRot += dx;
            
            float yAmp = ((yEnd - yStart) * PI) / (2 * moveTime);
            float dy = yAmp * sin(angle);
            yRot += dy;
            
            float zAmp = ((zEnd - zStart) * PI) / (2 * moveTime);
            float dz = zAmp * sin(angle);
            zRot += dz;
            
            currentTime++;
            if (currentTime == moveTime) {
                rotating = false;    
            }
        }
    } 
}





public abstract class Visualizer {
    final int TEXT_OFFSET = displayWidth - 200;
    final int TEXT_SEPARATION = 15;
    final int TEXT_SIZE = 14;
    final float TOTAL_SAMPLE_TIME = 1000;

    AudioInput input;
    AudioSource src;
    FFT fft;
    BeatDetect beat;
    Camera camera;
    int contrast;
    String name;
    boolean flashingMode;
    float volumeScale;
    boolean blur;
    float opacityFade;
    float samplerStartTime;
    float totalFrameRate;
    int frameRateSampleNum;
    
    // visualizers must return what their optimal frame rate is. this is so that
    // faster computers will not go crazy and update the visualizer way too fast
    public abstract int getOptimalFrameRate();
    
    // basic processing draw function, called every frame
    public abstract void draw();

    // the following 3 methods must implement 3 different views of the visualizer
    // by manually moving the camera (see Camera's initMoveCamera method).
    // these methods will be called with key presses 'f', 'r', and 't' respectively
    // NOTE: the logical handling of switching different views is handled in the
    // keyPressed() method of Visualizer, all these methods should ONLY implement the physical
    // moving of the camera.
    boolean frontView, rearView, topView;
    public abstract void frontView();
    public abstract void rearView();
    public abstract void topView();
    public abstract void autoPan();

    
    // implements particle mode (should just be switching boolean particles on/off)
    boolean particles;
    public abstract void particles();

    // particle mode can be a little too intense for some computers, so the first time
    // particle mode is called for each visualizer, Animus will sample 1000ms of 
    // the framerate (at the max particle num), then call adjustDetail, passing in the
    // average framerate. You can then use that info, in adjustDetail, to lower the number
    // of particles in a specific visualizers implementation of particle-mode
    boolean sampleParticleMode;
    public abstract void adjustDetail(float avgFr);

    // the following 3 methods must implement the 3 basic "drop levels" of a visualizer.
    // usually this is just switching the booleans highlight, expand, and revolve on/off,
    // then using these booleans in the code that draws the Visualizer to determine what
    // should be drawn every frame
    boolean highlight, expand, revolve, pause, followMouse;
    public abstract void highlight();
    public abstract void expand();
    public abstract void revolve();
    public abstract void pause();

    public void setup() {}
    
    Visualizer(AudioInput input, String name) {
        frontView = true;
        this.input = input;
        src = (AudioSource)input;
        fft = new FFT(input.bufferSize(), input.sampleRate());
        int sensitivity = 300;
        beat = new BeatDetect(input.bufferSize(), input.sampleRate());
        beat.setSensitivity(sensitivity);    
        camera = new Camera();
        this.name = name;
    }
    
    public void retrieveSound() {
        beat.detect(input.mix);
        fft.forward(input.mix);
        volumeScale = pow(10, sliderVal);
    }

    // calculates avg frame rate over TOTAL_SAMPLE_TIME. returns avg frame rate when done
    // sampling. returns 0 if still sampling. returns -1 if has already sampled.
    public float sampleFrameRate() {
        if (samplerStartTime == -1) {
            return -1;
        }

        if (samplerStartTime == 0) {
            samplerStartTime = millis();
        }

        if (samplerStartTime + TOTAL_SAMPLE_TIME >= millis()) {
            frameRateSampleNum++;
            totalFrameRate += frameRate;
            return -1;
        } else {
            samplerStartTime = -1;
            // println("avg particle framerate: " + totalFrameRate / frameRateSampleNum + " (" + name + ")");
            return totalFrameRate / frameRateSampleNum;
        }
    }

    // Call at the beginning of draw to setup background
    // backgroundColor is on gray scale from 0 to 255
    // opacity is on a scale from 0 to 255, where 0 is the max amt of blur, and
    // 255 is no blur at all
    public void setBackground(int backgroundColor, int opacity) {
        hint(DISABLE_DEPTH_TEST);
        noStroke();
        if (flashingMode && beat.isKick()) {
            contrast = 255 - contrast;
            backgroundColor = contrast;    
        }

        // flashingMode overrides opacity in order to create more blur
        if (flashingMode) {
            opacity = 10;
        }
        opacityFade = lerp(opacityFade, opacity, .05f);
        fill(backgroundColor, (int)opacityFade);
        rect(0, 0, width, height);
        hint(ENABLE_DEPTH_TEST);
        fill(255);
        if (backgroundColor == 0) {
            blendMode(SCREEN);
        } else {
            blendMode(DIFFERENCE);
        }
        hint(DISABLE_DEPTH_MASK);
    }
    
    // given an intensity, a peak (max intensity), and two ColorTrackers, calculates and returns an
    // array of colors, {red, green, blue, alpha} that represents the shift from the colors of the
    // baseTracker to the colors of the peakTracker. the alpha value is based on the instensity 
    // so that the baseTracker's colors will appear darker/fainter. ignore it as needed
    public float[] getColor(float intensity, int peak, ColorTracker baseTracker, ColorTracker peakTracker) {
        float red1 = baseTracker.red;
        float green1 = baseTracker.green;
        float blue1 = baseTracker.blue;
        float red2 = 255 - peakTracker.red;
        float green2 = 255 - peakTracker.green;
        float blue2 = 255 - peakTracker.blue;
        
        float shift2 = intensity / peak;
        float shift1 = 1 - shift2;
        
        float r = red1 * shift1 + red2 * shift2;
        float g = green1 * shift1 + green2 * shift2;
        float b = blue1 * shift1 + blue2 * shift2;
        float alpha = min(255 * shift2, 255);

        float[] result = {r, g, b, alpha};
        return result;
    }    

    public float bindRange(float k, float min, float max) {
        if (k < min) {
            return min;
        } else if (k > max) {
            return max;
        } else {
            return k;
        }
    }

    public void displayDebugText() {
        textSize(TEXT_SIZE);
        textAlign(LEFT, TOP);
        fill(255 - contrast);
        text("current frame rate: " + round(frameRate), 5, height - 25);    
        text(camera.pos.x + ", " + camera.pos.y + ", " + camera.pos.z, 5, height - 10);
    }

    // called by Animus (essentially main). since the displaying the help menu is global
    // to all visualizers, Animus handles that functionality and lets each Visualizer
    // know whether to display a help menu or not. we had to do it this way because
    // processing doesn't allow for static variables :(
    public void displayHelpMenu(boolean showInterface) {
        textSize(TEXT_SIZE);
        textAlign(LEFT, TOP);

        Map<String, Boolean> menuMap = new LinkedHashMap<String, Boolean>();
        menuMap.put("[h] hide interface", !showInterface);
        menuMap.put(" ", false);
        menuMap.put("Camera options:", false);
        menuMap.put("[a] auto panning mode", camera.autoPanningMode);
        menuMap.put("[v] free view mode", camera.viewingMode);
        menuMap.put("[f] front angle view", frontView);
        menuMap.put("[r] rear angle view", rearView);
        menuMap.put("[t] top-down view", topView);
        menuMap.put("  ", false);
        menuMap.put("Morph options:", false);
        menuMap.put("[1] highlight", highlight);
        menuMap.put("[2] expand", expand);
        menuMap.put("[3] revolve", revolve);
        menuMap.put("   ", false);
        menuMap.put("Screen options:", false);
        menuMap.put("[d] dark mode", contrast == 0);
        menuMap.put("[b] blur mode", blur);
        menuMap.put("[p] particle mode", particles);
        menuMap.put("[x] flashing mode", flashingMode);

        int i = 1;
        for (String textKey : menuMap.keySet()) {
            toggleTextColor(menuMap.get(textKey));
            text(textKey, TEXT_OFFSET, i * TEXT_SEPARATION);
            i++;
        }
    }

    public void toggleTextColor(boolean toggled) {
        if (toggled) {
            fill(255, 100, 100);
        } else {
            fill(abs(150-contrast), abs(150-contrast), abs(150-contrast));
        }
    }

    // returns intensity of a certain index within the bandsize, and scales it with volumeScale
    public float getIntensity(int index) {
        return abs(fft.getBand(index) * volumeScale * (PHI-1));
    }

    public float getGreatestMag(int maxFreq) {
        float greatestMag = 0;
        for (int i = 0; i < maxFreq; i++) {
            float tempMag = getIntensity(i);
            if (tempMag > greatestMag) {
                greatestMag = tempMag;    
            }    
        }
        return greatestMag;
    }

    public void fPressed(){
        if (frontView) return;
        camera.disableAllModes();
        frontView = !frontView;
        frontView();
        rearView = false;
        topView = false;
        followMouse = false;
    }

    public void aPressed(){
        camera.autoPanSwitch();
        camera.dirSwitch();
        autoPan();
        rearView = false;
        topView = false;
        frontView = false; 
        followMouse = false;       
    }
    public void rPressed(){
        if (rearView) return;
        camera.disableAllModes();
        rearView = !rearView;
        rearView();
        topView = false;
        frontView = false;
        followMouse = false;
    }

    public void tPressed(){
        if (topView) return;
        camera.disableAllModes();
        topView = !topView;
        topView();
        rearView = false;
        frontView = false;    
        followMouse = false;    
    }

    public void mPressed(){
        followMouse = !followMouse;
        // camera.viewSwitch();
        camera.disableAllModes();
        rearView = false;
        topView = false;
        frontView = false;
        if (!followMouse) {
            if (this instanceof Droplet) {
                aPressed();
            } else {
                fPressed();
            }
        }
    }

    public void keyPressed() {
        switch (key) {
            // showInterface toggle handled in Animus due to not being able to
            // use static variables (processing fucking sucks!)
            case ' ':
                pause();
                followMouse = pause;
                mouseX = width/2;
                mouseY = height/2;
                break;
            // invert toggle handled in Animus
            case 'm':
                mPressed();
                break;
            case 's':
                aPressed();
                break;
            case 'a':
                fPressed();
                break;
            case 'd':
                rPressed();
                break;
            case 'w':
                tPressed();
                break;
            case 'b':
                blur = !blur;
                break;
            case 'M':
                mPressed();
                break;
            case 'S':
                aPressed();
                break;
            case 'A':
                fPressed();
                break;
            case 'D':
                rPressed();
                break;
            case 'W':
                tPressed();
                break;
            case 'B':
                blur = !blur;
                break;                
            case '1':
                highlight();
                break;
            case '2':
                expand();
                break;
            case '3':
                revolve(); 
                break;
            case '4':
                particles();
                if (!sampleParticleMode) {
                    sampleParticleMode = true;
                }
                break;
            default:
                break;
        }
    }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "animus" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
