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
import java.util.stream.Collectors;

public class Main extends PApplet {
    private final AtomicBoolean imageLoadEnded = new AtomicBoolean(false);
    private EnumMap<CardKind, CardLoader.CardInfo<PImage>> cardInfos = new EnumMap<>(CardKind.class);
    private RealCard selectedCard = null;
    private PFont cardTextFont = null;

    private List<CardGeometry> cardGeometries = new ArrayList<>();

    public static void main(String[] args) {
        Property.getInstance().setLanguage("Japanese");
        PApplet.main("snowesamosc.mtgturing.Main");
    }

    @Override
    public void settings() {
        this.size((int) (this.displayWidth * 0.7), (int) (this.displayHeight * 0.7));
    }

    @Override
    public void setup() {
        var prop = Property.getInstance();

        new Thread(() -> {
            this.cardInfos = CardLoader.loadAllCard(EnumSet.allOf(CardKind.class), prop.getLanguage(), PImage::new);
            this.imageLoadEnded.set(true);
        }).start();
        new Thread(() -> {
            this.cardTextFont = this.createFont("HGPｺﾞｼｯｸE 標準", 15);
            System.out.println("Font was loaded.");
        }).start();

        var game = Game.getInstance();
        var attachList = new ArrayList<AttachInfo>();

        game.init(
                this.createBob(attachList),
                this.createAlice(attachList),
                System.out::println
        );

        attachList.forEach(attach -> game.attach(attach.getMain(), attach.getSub()));
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
                var offsetY = this.getOpPanelWidth() * this.getCardAspectRatio() + 15;
                this.text(name, 0, offsetY);
                offsetY += 15;
                if (!this.selectedCard.getCreatureTypes().isEmpty()) {
                    this.text(this.selectedCard.getCreatureTypes().stream()
                            .map(type -> Property.getInstance().translate(type))
                            .collect(Collectors.joining(" ")), 0, offsetY);
                    offsetY += 15;
                }
                this.text(text, 0, offsetY, this.getOpPanelWidth(), this.height - offsetY);
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
            AtomicReference<CardKind> beforeCardType = new AtomicReference<>(null);
            bob.field().stream().filter(card -> !game.isAttachSub(card)).sorted(Comparator.comparing(RealCard::getType)).forEach(
                    card -> {
                        var type = card.getType();
                        var deltaX = (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                        game.attachedCard(card).ifPresent(sub -> this.cardGeometries.add(new CardGeometry(sub,
                                new Rectangle2D.Float(lastX.get() + deltaX, this.getCardHeight() / 10F, this.getCardWidth(), this.getCardHeight()))));
                        if (!card.isTapped()) {
                            this.cardGeometries.add(new CardGeometry(card,
                                    new Rectangle2D.Float(lastX.get() + deltaX, 0, this.getCardWidth(), this.getCardHeight())));
                        } else {
                            this.cardGeometries.add(new CardGeometry(card,
                                    new Rectangle2D.Float(lastX.get() + deltaX, this.getCardHeight() - this.getCardWidth(), this.getCardHeight(), this.getCardWidth())));
                        }
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
            AtomicReference<CardKind> beforeCardType = new AtomicReference<>(null);
            alice.field().stream().filter(card -> !game.isAttachSub(card)).sorted(Comparator.comparing(RealCard::getType)).forEach(
                    card -> {
                        var type = card.getType();
                        var deltaX = (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                        game.attachedCard(card).ifPresent(sub -> this.cardGeometries.add(new CardGeometry(sub,
                                new Rectangle2D.Float(lastX.get() + deltaX - this.getCardWidth() / 20F, this.height - 2 * this.getCardHeight() - this.getCardHeight() / 10F, this.getCardWidth(), this.getCardHeight()))));
                        if (!card.isTapped()) {
                            this.cardGeometries.add(new CardGeometry(card,
                                    new Rectangle2D.Float(lastX.get() + deltaX, this.height - 2 * this.getCardHeight(),
                                            this.getCardWidth(), this.getCardHeight())));
                        } else {
                            this.cardGeometries.add(new CardGeometry(card,
                                    new Rectangle2D.Float(lastX.get() + deltaX, this.height - this.getCardHeight() - this.getCardWidth(),
                                            this.getCardHeight(), this.getCardWidth())));
                        }
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
        this.pushMatrix();
        this.translate(x, y);
        if (card.isTapped()) {
            this.translate(this.getCardHeight(), 0);
            this.rotate(PI / 2);
        }
        this.image(this.cardInfos.get(card.getType()).mappedImage(), 0, 0, this.getCardWidth(), this.getCardHeight());
        this.popMatrix();
    }

    private Player createAlice(List<AttachInfo> attachList) {
        var fields = new ArrayList<RealCard>();
        attachList.forEach(attach -> {
            var card = RealCard.createCard(CardKind.CloakOfInvisibility);
            attach.setSub(card);
            fields.add(card);
        });
        fields.add(RealCard.createCard(CardKind.WheelOfSunAndMoon));
        fields.add(RealCard.createCard(CardKind.IllusoryGains));
        fields.add(RealCard.createCard(CardKind.SteelyResolve));
        fields.add(RealCard.createCard(CardKind.DreadOfNight));
        fields.add(RealCard.createCard(CardKind.DreadOfNight));
        fields.add(RealCard.createCard(CardKind.FungusSliver));
        fields.add(RealCard.createCard(CardKind.RotlungReanimator));
        fields.add(RealCard.createCard(CardKind.SharedTriumph));
        fields.add(RealCard.createCard(CardKind.RotlungReanimator));
        fields.add(RealCard.createCard(CardKind.SharedTriumph));

        fields.addAll(RealCard.createCards(List.of(
                CardKind.Vigor, CardKind.MesmericOrb, CardKind.PrismaticOmen,
                CardKind.Choke, CardKind.BlazingArchon)));
        {
            var c = RealCard.createCard(CardKind.AncientTomb);
            c.tap();
            fields.add(c);
        }

        return new Player(RealCard.createCards(List.of(
                CardKind.CleansingBeam,
                CardKind.CoalitionVictory,
                CardKind.SoulSnuffers
        )),
                RealCard.createCards(List.of(
                        CardKind.Infest
                )),
                fields);
    }

    private Player createBob(List<AttachInfo> attachRequire) {
        var fields = new ArrayList<RealCard>();
        for (int i = 0; i < 29; i++) {
            var c = RealCard.createCard(CardKind.RotlungReanimator);
            attachRequire.add(new AttachInfo(c));
            fields.add(c);
        }
        for (int i = 0; i < 7; i++) {
            var c = RealCard.createCard(CardKind.XathridNecromancer);
            attachRequire.add(new AttachInfo(c));
            fields.add(c);
        }
        fields.add(RealCard.createCard(CardKind.RotlungReanimator));
        fields.add(RealCard.createCard(CardKind.RotlungReanimator));
        fields.addAll(RealCard.createCards(List.of(
                CardKind.WildEvocation, CardKind.Recycle,
                CardKind.PrivilegedPosition, CardKind.Vigor,
                CardKind.BlazingArchon
        )));

        return new Player(List.of(), List.of(), fields);
    }

    private record CardGeometry(RealCard card, Rectangle2D.Float geometry) {
    }

    private static class AttachInfo {
        private final RealCard main;
        private RealCard sub;

        public AttachInfo(RealCard main) {
            this.main = main;
        }

        public RealCard getMain() {
            return this.main;
        }

        public RealCard getSub() {
            return this.sub;
        }

        public void setSub(RealCard sub) {
            this.sub = sub;
        }
    }
}
