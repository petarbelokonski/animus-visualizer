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
PImage sprite, glow, glowBig, glowBig2;

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
CheckBox highlight, expand, revolve, particles, front, rear, top, autoPan, viewing, blur, invert, ring, fluid, droplet, name, animation, background;
Textlabel interfaceLabel;
float sliderVal = 0.5;
PImage logo;
PFont font, nameFont;
boolean load, showInterface, debugMode, showName, showAnimation, showBackground;
float interfaceT;
int contrast;
PImage cam, modeBackground;

// switch between names by pressing 'l'
int nameIndex = 0;
String[] names = {"Purple Banana Syndicate", "e:Lee", "Arks", "Xylophobe", "digitalNightmarez", "Funk Aesthetics", "SECHNIA", "RawDope Bass 2"};

BeatDetect beat;
Movie myMovie;

FFT fft;
float[] spectrum = new float[512];
int increment = 0;
float lastLevel, level;

float opacityFade;


public void settings() {
  size(displayWidth, displayHeight, P3D);
//   fullScreen(P3D, SPAN);
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

    buttons = new CheckBox[17];
    buttonLabels = new Textlabel[17];
    cp5 = new ControlP5(this);
    guiSetup(cFont);
    visualizers[select].setup();


    background(255, 204, 230);    
    beat = new BeatDetect(input.bufferSize(), input.sampleRate());
    nameFont = createFont("agency-fb.ttf",256,true);
    myMovie = new Movie(this, "cover5.mov");
    myMovie.loop();
    fft = new FFT(input.bufferSize(), input.sampleRate());
}

/// TODO:
//
/// - video overlay color when animation 
/// - make video shorter to use less memo
/// - experiment with different video styles /maybe switch between a few of them
/// - draw lines positioned randomly with the text which sized also based on equalizer
/// - fix missing mic sensitivity mode
/// - fix name dont showing 
//

void draw() {
    showInterfaceIfNeeded();
    showBackgroundIfNeeded();
    showAnimationIfNeeded();
    showNameIfNeeded();
}

 void setBackground(int backgroundColor, int opacity) {
        hint(DISABLE_DEPTH_TEST);
        noStroke();

        opacityFade = lerp(opacityFade, opacity, .05);
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

void showBackgroundIfNeeded() {
    if (showBackground) {
        // TODO: add "overlay effect" so that video looks with a blur overlay like the animation
        // rect(0, 0, width, height);
        // hint(ENABLE_DEPTH_TEST);
        // fill(255);
        // blendMode(DIFFERENCE);
        // hint(DISABLE_DEPTH_MASK);
        image(myMovie, 0, 0, width, height);
    }
}



void showNameIfNeeded() {
    if (showName) {
        textFont(nameFont, 100+400*input.mix.level());
        beat.detect(input.mix);
        String name = names[nameIndex];
        // if (increment%8==0){
        println("level: "+input.mix.level());
        fill(255,255-10*(input.mix.level()*100),255-4*(input.mix.level()*100)); 
        text(name, displayWidth*1/2, displayHeight*1/2);
        // } else {
        //     text(name, displayWidth*1/2, displayHeight*1/2);
        // }
        // if(increment%32==0){
        //     fill(15,18,51); 
        //     text(name, displayWidth*1/2, displayHeight*1/2);
        // }

        level = input.mix.level();
        
        float dif = abs(abs(level) - abs(lastLevel));
        if ((level*40)/100 > dif) {
            textFont(nameFont, displayWidth/4);
            fill(0,255,0,80);  
            text(name, displayWidth*1/2, displayHeight*1/2);
        }

        if (increment % 4 == 0 ) {
            lastLevel = level;
        }


        if (level>0.1 && level<0.2){
            textFont(nameFont, displayWidth/4);
            fill(255,255,255,80);  
            text(name, displayWidth*1/2, displayHeight*1/2);
        }
        if (level>0.2 && level<0.25){
            textFont(nameFont, displayWidth/12);
            fill(255,255,255,57);  
            pushMatrix();
            translate(displayWidth/2,displayHeight/2);
            scale(1, -1);
            text(name, displayWidth*1/2, displayHeight/2);
            popMatrix();
        }
        if (level>0.25 && level<0.3){
            textFont(nameFont, displayWidth/3);
            fill(255,255-10*(input.mix.level()*100),255-4*(input.mix.level()*100), 57); 
            pushMatrix();
            translate(displayWidth,displayHeight);
            scale(1, -1);
            text(name, displayWidth*1/2, displayHeight/4);
            popMatrix();
        }
        if (level>0.4 && level<0.5){
            textFont(nameFont, displayWidth/2);
            fill(15,18,51);
            pushMatrix();
            scale(1, -1);
            text(name, displayWidth, displayHeight-200);
            popMatrix();
        }
        increment+=2;
        if (increment >= input.bufferSize() ){
            increment = 0;
        } 
        noLights();
        updateGui();
    }
}

void showAnimationIfNeeded() {
    if (showAnimation) {
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

        if (visualizers[select].sampleParticleMode) {
            float avgFr = visualizers[select].sampleFrameRate();
            if (avgFr > 0) {
                visualizers[select].adjustDetail(avgFr);
            }
        }
    } 
}
void showInterfaceIfNeeded() {
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
    volumeBar.visible = showInterface;
}
// Called every time a new frame is available to read
void movieEvent(Movie m) {
  m.read();
}

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
}

void guiSetup(ControlFont font){
    volumeBar = new VolumeBar(displayWidth - 500, TEXT_OFFSET+69, "VolumeBackground.png", "VolumeMid.png", "VolumeEnd.png", "VolumeBackgroundI.png", "VolumeMidI.png", "VolumeEndI.png");
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
    buttons[16] = background = cp5.addCheckBox("background").addItem("background [m]", 0);
    buttonLabels[16] = cp5.addTextlabel("backgroundT").setText("Background [m]");
    
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
        showName = !showName;
    } else if (theEvent.isFrom(animation)) {
        showAnimation = !showAnimation;
    } else if (theEvent.isFrom(background)) {
        showBackground = !showBackground;
    }
}

void keyPressed() {
    switch (key) {
        case 'D':
            debugMode = !debugMode;
            break;
        case 'h':
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
        case 'n':
            showName = !showName;
            break;
        case 'v':
            showAnimation = !showAnimation;
            break;
        case 'l':
            nameIndex++;
            if (nameIndex == names.length) {
                nameIndex = 0;
            }
            println("Currently selected is: "+names[nameIndex]);
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
