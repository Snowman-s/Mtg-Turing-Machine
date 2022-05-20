package snowesamosc.mtgturing;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.MouseEvent;
import snowesamosc.mtgturing.cards.*;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Main extends PApplet {
    private final AtomicBoolean loadEnded = new AtomicBoolean(false);
    private EnumMap<CardKind, CardLoader.CardInfo<PImage>> cardInfos = new EnumMap<>(CardKind.class);
    private RealCard selectedCard = null;
    private PFont cardTextFont = null;
    private List<ControllerLoader.Table2Element> controllerInfo = null;

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
            var count = new CountDownLatch(3);
            new Thread(() -> {
                this.cardInfos = CardLoader.loadAllCard(EnumSet.allOf(CardKind.class), prop.getLanguage(), PImage::new);
                count.countDown();
            }).start();
            new Thread(() -> {
                this.cardTextFont = this.createFont("HGPｺﾞｼｯｸE 標準", 15);
                System.out.println("Font was loaded.");
                count.countDown();
            }).start();
            new Thread(() -> {
                this.controllerInfo = ControllerLoader.loadTable2();
                System.out.println("Controller data was loaded.");
                count.countDown();
            }).start();

            try {
                count.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            var game = Game.getInstance();
            var attachList = new ArrayList<AttachInfo>();

            game.init(
                    this.createBob(attachList),
                    this.createAlice(attachList),
                    System.out::println
            );

            attachList.forEach(attach -> game.attach(attach.getMain(), attach.getSub()));

            System.out.println("Game was initialized.");

            this.loadEnded.set(true);
        }).start();
    }

    @Override
    public void draw() {
        this.background(0);
        if (!this.loadEnded.get()) {
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
                var offsetY = new AtomicReference<>(this.getOpPanelWidth() * this.getCardAspectRatio() + 15);
                this.text(name, 0, offsetY.get());
                offsetY.set(offsetY.get() + 15);
                if (!this.selectedCard.getCreatureTypes().isEmpty()) {
                    this.text(this.selectedCard.getCreatureTypes().stream()
                            .map(type -> Property.getInstance().translate(type))
                            .collect(Collectors.joining(" ")), 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                }
                this.textSize(13);
                this.selectedCard.getReplaceColors().stream()
                        .map(replaceColor -> "(" + Property.getInstance().translate(replaceColor.component1()) +
                                " → " + Property.getInstance().translate(replaceColor.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );
                this.selectedCard.getReplaceTypes().stream()
                        .map(replaceType -> "(" + Property.getInstance().translate(replaceType.component1()) +
                                " → " + Property.getInstance().translate(replaceType.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );

                this.textSize(15);
                this.text(text, 0, offsetY.get(), this.getOpPanelWidth(), this.height - offsetY.get());
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
        if (!card.isPhaseIn()) {
            this.tint(100, 100, 100);
        }
        this.image(this.cardInfos.get(card.getType()).mappedImage(), 0, 0, this.getCardWidth(), this.getCardHeight());
        this.noTint();
        this.popMatrix();
    }

    private Player createAlice(List<AttachInfo> attachList) {
        var fields = new ArrayList<RealCard>();
        attachList.forEach(attach -> {
            var card = RealCard.createCard(CardKind.CloakOfInvisibility);
            attach.setSub(card);
            card.setPhaseIn(attach.getMain().isPhaseIn());
            fields.add(card);
        });
        fields.add(RealCard.createCard(CardKind.WheelOfSunAndMoon));
        fields.add(RealCard.createCard(CardKind.IllusoryGains));
        {
            RealCard card = RealCard.createCard(CardKind.SteelyResolve);
            card.asThatCard(SteelyResolve.class, c -> c.setSelectedType(CreatureType.AssemblyWorker));
            fields.add(card);
        }
        for (int i = 0; i < 2; i++) {
            RealCard card = RealCard.createCard(CardKind.DreadOfNight);
            card.asThatCard(DreadOfNight.class, c -> c.setMinusColor(CardColor.Black));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.FungusSliver);
            card.asThatCard(FungusSliver.class, c -> c.setPlusType(CreatureType.Incarnation));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Lhurgoyf);
                c.setCreateColor(CardColor.Black);
                c.setCreateType(CreatureType.Cephalid);
            });
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.SharedTriumph);
            card.asThatCard(SharedTriumph.class, c -> c.setSelectedType(CreatureType.Lhurgoyf));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Rat);
                c.setCreateColor(CardColor.Black);
                c.setCreateType(CreatureType.Cephalid);
            });
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.SharedTriumph);
            card.asThatCard(SharedTriumph.class, c -> c.setSelectedType(CreatureType.Rat));
            fields.add(card);
        }

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

        this.controllerInfo.forEach(element -> {
            var card = RealCard.createCard(element.kind());
            attachRequire.add(new AttachInfo(card));
            card.asThatCard(XathridNecromancer.class, c -> {
                c.setDieType(element.dieType());
                c.setCreateColor(element.createColor());
                c.setCreateType(element.createType());
                c.setPhaseIn(element.phaseIn());
            });
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(element.dieType());
                c.setCreateColor(element.createColor());
                c.setCreateType(element.createType());
                c.setPhaseIn(element.phaseIn());
            });
            fields.add(card);
        });

        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Lhurgoyf);
                c.setCreateColor(CardColor.Green);
                c.setCreateType(CreatureType.Lhurgoyf);
            });
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Rat);
                c.setCreateColor(CardColor.White);
                c.setCreateType(CreatureType.Rat);
            });
            fields.add(card);
        }

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
