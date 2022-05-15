package snowesamosc.mtgturing;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Main extends PApplet {
    private final AtomicBoolean imageLoadEnded = new AtomicBoolean(false);
    private EnumMap<CardList, PImage> images = new EnumMap<>(CardList.class);

    public static void main(String[] args) {
        PApplet.main("snowesamosc.mtgturing.Main");
    }

    @Override
    public void settings() {
        this.size((int) (this.displayWidth * 0.5), (int) (this.displayHeight * 0.5));
    }

    @Override
    public void setup() {
        new Thread(() -> {
            this.images = ImageLoader.loadAllCardImage(EnumSet.allOf(CardList.class), "Japanese", PImage::new);
            this.imageLoadEnded.set(true);
        }).start();
    }

    @Override
    public void draw() {
        this.background(0);
        if (!this.imageLoadEnded.get()) {
            this.pushStyle();
            this.noFill();
            this.stroke(255);
            this.strokeWeight(5);
            float radius = min(this.width / 2F, this.height / 2F);
            this.arc(this.width / 2F, this.height / 2F, radius, radius,
                    this.frameCount / 30F, this.frameCount / 30F + PI / 2);
            this.popStyle();
            return;
        }
        var keyList = this.images.keySet().stream()
                .sorted(Comparator.comparing(CardList::getOriginalName))
                .collect(Collectors.toList());
        int i = 0;
        for (var key : keyList) {
            this.image(this.images.get(key), i * 20, 0);
            i++;
        }
    }
}
