package snowesamosc;

import processing.core.PApplet;
import processing.core.PImage;

public class Main extends PApplet {
    private PImage image;

    public static void main(String[] args) {
        PApplet.main("snowesamosc.Main");
    }

    @Override
    public void settings() {
        this.size(500, 500);
    }

    @Override
    public void setup() {
        new Thread(() -> this.image =
                new PImage(ImageLoader.loadCardImage("Rotlung Reanimator", "Japanese"))).start();
    }

    @Override
    public void draw() {
        this.background(0);
        if (this.image != null) this.image(this.image, 0, 0);
    }
}
