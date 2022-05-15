package snowesamosc;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main extends PApplet {
    private final Map<CardList, PImage> images = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        PApplet.main("snowesamosc.Main");
    }

    @Override
    public void settings() {
        this.size(500, 500);
    }

    @Override
    public void setup() {
        for (var v : CardList.values()) {
            var cardName = v.getOriginalName();
            new Thread(() -> this.images.put(v,
                    new PImage(ImageLoader.loadCardImage(cardName, "Japanese")))).start();
        }
    }

    @Override
    public void draw() {
        this.background(0);
        synchronized (this.images) {
            int i = 0;
            for (var e : this.images.entrySet()) {
                this.image(e.getValue(), i * 20, 0);
                i++;
            }
        }
    }
}
