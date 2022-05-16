package snowesamosc.mtgturing;

import processing.core.PApplet;
import processing.core.PImage;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends PApplet {
    private final AtomicBoolean imageLoadEnded = new AtomicBoolean(false);
    private EnumMap<CardType, PImage> images = new EnumMap<>(CardType.class);

    public static void main(String[] args) {
        PApplet.main("snowesamosc.mtgturing.Main");
    }

    @Override
    public void settings() {
        this.size((int) (this.displayWidth * 0.7), (int) (this.displayHeight * 0.7));
    }

    @Override
    public void setup() {
        new Thread(() -> {
            this.images = ImageLoader.loadAllCardImage(EnumSet.allOf(CardType.class), "Japanese", PImage::new);
            this.imageLoadEnded.set(true);
        }).start();

        Game.getInstance().init(
                createBob(),
                createAlice(),
                System.out::println
        );
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
        var game = Game.getInstance();
        var bob = game.getBob();
        var alice = game.getAlice();

        this.pushStyle();
        this.noFill();
        this.stroke(255);
        this.line(0, this.height * 2F / 5F, this.width, this.height * 2F / 5F);
        this.line(0, this.height * 4F / 5F, this.width, this.height * 4F / 5F);
        this.popStyle();

        //操作パネル
        this.pushStyle();
        this.fill(0);
        this.stroke(255);
        this.rect(0, 0, getOpPanelWidth(), height);
        this.popStyle();

        final float caX = getOpPanelWidth(); //cardAreaX

        this.pushStyle();
        //bob
        {
            AtomicReference<Float> lastX = new AtomicReference<>(caX- getCardWidth() * 0.8F);
            AtomicReference<CardType> beforeCardType = new AtomicReference<>(null);
            bob.field().forEach(
                card -> {
                    var type = card.getType();
                    var deltaX =  (beforeCardType.get() == type ? getCardWidth() / 10F: getCardWidth() * 0.8F);
                    renderCard(card, lastX.get() + deltaX, 0);
                    lastX.set(lastX.get() + deltaX);
                    beforeCardType.set(type);
                }
            );
        }
        //Alice
        {
            AtomicInteger handsCount = new AtomicInteger();
            alice.hands().forEach(
                    card -> {
                        renderCard(card, caX + handsCount.get() * getCardWidth(), height - getCardHeight());
                        handsCount.getAndIncrement();
                    }
            );
        }
        {
            AtomicReference<Float> lastX = new AtomicReference<>(caX - getCardWidth() * 0.8F);
            AtomicReference<CardType> beforeCardType = new AtomicReference<>(null);
            alice.field().stream().sorted(Comparator.comparing(RealCard::getType)).forEach(
                    card -> {
                        var type = card.getType();
                        if(type == CardType.CloakOfInvisibility) return;
                        var deltaX =  (beforeCardType.get() == type ? getCardWidth() / 10F: getCardWidth() * 0.8F);
                        renderCard(card, lastX.get() + deltaX, height - 2 * getCardHeight());
                        lastX.set(lastX.get() + deltaX);
                        beforeCardType.set(type);
                    }
            );
        }
        this.popStyle();
    }

    private float getCardWidth(){
        return (height / 5F) / 1.396F;
    }
    private float getCardHeight(){
        return height / 5F;
    }
    private float getOpPanelWidth(){
        return width / 8F;
    }
    private void renderCard(RealCard card, float x, float y){
        image(images.get(card.getType()), x, y, getCardWidth(), getCardHeight());
    }

    private Player createAlice(){
        var fields = new ArrayList<RealCard>();
        for (int i = 0; i < 29; i++) {
            fields.add(RealCard.createCard(CardType.CloakOfInvisibility));
        }
        for (int i = 0; i < 7; i++) {
            fields.add(RealCard.createCard(CardType.CloakOfInvisibility));
        }
        fields.add(RealCard.createCard(CardType.WheelOfSunAndMoon));
        fields.add(RealCard.createCard(CardType.IllusoryGains));
        fields.add(RealCard.createCard(CardType.SteelyResolve));
        fields.add(RealCard.createCard(CardType.DreadOfNight));
        fields.add(RealCard.createCard(CardType.DreadOfNight));
        fields.add(RealCard.createCard(CardType.FungusSliver));
        fields.add(RealCard.createCard(CardType.RotlungReanimator));
        fields.add(RealCard.createCard(CardType.SharedTriumph));
        fields.add(RealCard.createCard(CardType.RotlungReanimator));
        fields.add(RealCard.createCard(CardType.SharedTriumph));

        fields.addAll(RealCard.createCards(List.of(
                CardType.Vigor, CardType.MesmericOrb,
                CardType.AncientTomb,CardType.PrismaticOmen,
                CardType.Choke,CardType.BlazingArchon)));

        return new Player(RealCard.createCards(List.of(
                CardType.CleansingBeam,
                CardType.CoalitionVictory,
                CardType.SoulSnuffers
        )),
                RealCard.createCards(List.of(
                        CardType.Infest
                )),
                fields);
    }

    private Player createBob() {
        var fields = new ArrayList<RealCard>();
        for (int i = 0; i < 29; i++) {
            fields.add(RealCard.createCard(CardType.RotlungReanimator));
        }
        for (int i = 0; i < 7; i++) {
            fields.add(RealCard.createCard(CardType.XathridNecromancer));
        }
        fields.add(RealCard.createCard(CardType.RotlungReanimator));
        fields.add(RealCard.createCard(CardType.RotlungReanimator));
        fields.addAll(RealCard.createCards(List.of(
                CardType.WildEvocation, CardType.Recycle,
                CardType.PrivilegedPosition, CardType.Vigor,
                CardType.BlazingArchon
        )));

        return new Player(List.of(), List.of(), fields);
    }
}
