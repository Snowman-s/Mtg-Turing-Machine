package snowesamosc.mtgturing;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.MouseEvent;
import snowesamosc.mtgturing.cards.RealCard;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends PApplet {
    private final AtomicBoolean imageLoadEnded = new AtomicBoolean(false);
    private EnumMap<CardType, CardLoader.CardInfo<PImage>> cardInfos = new EnumMap<>(CardType.class);
    private RealCard selectedCard = null;
    private PFont cardTextFont = null;

    private List<CardGeometry> cardGeometries = new ArrayList<>();

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
            this.cardInfos = CardLoader.loadAllCard(EnumSet.allOf(CardType.class), "Japanese", PImage::new);
            this.imageLoadEnded.set(true);
        }).start();
        new Thread(() -> {
            this.cardTextFont = this.createFont("HGPｺﾞｼｯｸE 標準", 15);
            System.out.println("Font was loaded.");
        }).start();

        Game.getInstance().init(
                this.createBob(),
                this.createAlice(),
                System.out::println
        );
    }

    @Override
    public void draw() {
        this.background(0);
        if (!this.imageLoadEnded.get() || this.cardTextFont == null) {
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
        this.rect(0, 0, this.getOpPanelWidth(), this.height);
        if (this.selectedCard != null) {
            this.pushStyle();
            var cardInfo = this.cardInfos.get(this.selectedCard.getType());
            this.image(cardInfo.mappedImage(),
                    0, 0, this.getOpPanelWidth(), this.getOpPanelWidth() * this.getCardAspectRatio());
            this.fill(255);
            this.textFont(this.cardTextFont);
            {
                var name = cardInfo.cardName();
                var text = cardInfo.cardText();
                this.text(name, 0, this.getOpPanelWidth() * this.getCardAspectRatio() + 15);
                this.text(text, 0, this.getOpPanelWidth() * this.getCardAspectRatio() + 30,
                        this.getOpPanelWidth(), this.height - this.getOpPanelWidth() * this.getCardAspectRatio());
            }
            this.popStyle();
        }
        this.popStyle();

        this.setCardGeometries();

        this.pushStyle();
        this.cardGeometries.forEach(i -> this.renderCard(i.card, i.geometry.x, i.geometry.y));
        this.popStyle();
    }

    private void setCardGeometries() {
        var game = Game.getInstance();
        var bob = game.getBob();
        var alice = game.getAlice();
        this.cardGeometries = new ArrayList<>();
        final float caX = this.getOpPanelWidth(); //cardAreaX
        {
            AtomicReference<Float> lastX = new AtomicReference<>(caX - this.getCardWidth() * 0.8F);
            AtomicReference<CardType> beforeCardType = new AtomicReference<>(null);
            bob.field().forEach(
                    card -> {
                        var type = card.getType();
                        var deltaX = (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                        this.cardGeometries.add(new CardGeometry(card,
                                new Rectangle2D.Float(lastX.get() + deltaX, 0, this.getCardWidth(), this.getCardHeight())));
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
                        this.cardGeometries.add(new CardGeometry(card,
                                new Rectangle2D.Float(caX + handsCount.get() * this.getCardWidth(),
                                        this.height - this.getCardHeight(),
                                        this.getCardWidth(), this.getCardHeight())));
                        handsCount.getAndIncrement();
                    }
            );
        }
        {
            AtomicReference<Float> lastX = new AtomicReference<>(caX - this.getCardWidth() * 0.8F);
            AtomicReference<CardType> beforeCardType = new AtomicReference<>(null);
            alice.field().stream().sorted(Comparator.comparing(RealCard::getType)).forEach(
                    card -> {
                        var type = card.getType();
                        if (type == CardType.CloakOfInvisibility) return;
                        var deltaX = (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                        this.cardGeometries.add(new CardGeometry(card,
                                new Rectangle2D.Float(lastX.get() + deltaX, this.height - 2 * this.getCardHeight(),
                                        this.getCardWidth(), this.getCardHeight())));
                        lastX.set(lastX.get() + deltaX);
                        beforeCardType.set(type);
                    }
            );
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        var cardListCopy = new ArrayList<>(this.cardGeometries);
        Collections.reverse(cardListCopy);
        var optional = cardListCopy.stream()
                .filter(cardGeometry -> cardGeometry.geometry.x < this.mouseX &&
                        this.mouseX < cardGeometry.geometry.x + cardGeometry.geometry.width &&
                        cardGeometry.geometry.y < this.mouseY &&
                        this.mouseY < cardGeometry.geometry.y + cardGeometry.geometry.height)
                .findFirst()
                .map(CardGeometry::card);
        if (optional.isEmpty()) {
            this.selectedCard = null;
            return;
        }
        this.selectedCard = optional.get();
    }

    private float getCardWidth() {
        return this.getCardHeight() / this.getCardAspectRatio();
    }

    private float getCardAspectRatio() {
        return 1.396F;
    }

    private float getCardHeight() {
        return this.height / 5F;
    }

    private float getOpPanelWidth() {
        return this.width / 8F;
    }

    private void renderCard(RealCard card, float x, float y) {
        this.image(this.cardInfos.get(card.getType()).mappedImage(), x, y, this.getCardWidth(), this.getCardHeight());
    }

    private Player createAlice() {
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

    private record CardGeometry(RealCard card, Rectangle2D.Float geometry) {
    }
}
