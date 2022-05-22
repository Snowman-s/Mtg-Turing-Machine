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
    private PImage tokenImage = null;
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
            var count = new CountDownLatch(4);
            new Thread(() -> {
                this.cardInfos = CardLoader.loadAllCard(EnumSet.allOf(CardKind.class), prop.getLanguage(), PImage::new);
                count.countDown();
            }).start();
            new Thread(() -> {
                this.tokenImage = TokenLoader.loadTokenImage(PImage::new);
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
        var prop = Property.getInstance();

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

            PImage image;
            String name, text;
            {
                if (this.selectedCard.isToken()) {
                    image = this.tokenImage;
                    name = "Token";
                    text = "";
                } else {
                    var cardInfo = this.cardInfos.get(this.selectedCard.getType());
                    image = cardInfo.mappedImage();
                    name = cardInfo.cardName();
                    text = cardInfo.cardText();
                }
            }

            this.image(image,
                    0, 0, this.getOpPanelWidth(), this.getOpPanelWidth() * this.getCardAspectRatio());
            this.fill(255);
            this.textFont(this.cardTextFont);
            {
                var offsetY = new AtomicReference<>(this.getOpPanelWidth() * this.getCardAspectRatio() + 15);
                this.text(name, 0, offsetY.get());
                offsetY.set(offsetY.get() + 15);

                if (!this.selectedCard.getColors().isEmpty()) {
                    this.pushStyle();
                    this.textSize(13);
                    var colorText = this.selectedCard.getColors().stream()
                            .map(prop::translate)
                            .collect(Collectors.joining());
                    this.text(colorText, 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                    this.popStyle();
                }

                if (!this.selectedCard.getCreatureTypes().isEmpty()) {
                    this.text(this.selectedCard.getCreatureTypes().stream()
                            .map(prop::translate)
                            .collect(Collectors.joining(" ")), 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                }
                this.textSize(13);
                this.selectedCard.getReplaceColors().stream()
                        .map(replaceColor -> "(" + prop.translate(replaceColor.component1()) +
                                " → " + prop.translate(replaceColor.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );
                this.selectedCard.getReplaceTypes().stream()
                        .map(replaceType -> "(" + prop.translate(replaceType.component1()) +
                                " → " + prop.translate(replaceType.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );
                this.selectedCard.getSelectedType().ifPresent(
                        type -> {
                            this.text("★" + prop.translate(type), 0, offsetY.get());
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

        Comparator<RealCard> comparator = (a, b) ->
                Comparator.nullsLast(Comparator.comparing((CardKind t) -> t))
                        .compare(a.getType(), b.getType());

        this.cardGeometries = new ArrayList<>();
        final float caX = this.getOpPanelWidth(); //cardAreaX
        {
            AtomicReference<Float> lastNormalX = new AtomicReference<>(caX - this.getCardWidth() * 0.8F);
            AtomicReference<Float> lastTokenX = new AtomicReference<>(caX - this.getCardWidth() / 10F);
            AtomicReference<CardKind> beforeCardType = new AtomicReference<>(null);
            bob.field().stream()
                    .filter(card -> !game.isAttachSub(card))
                    .sorted(comparator)
                    .forEach(
                            card -> {
                                var type = card.getType();
                                var deltaX = card.isToken() ?
                                        this.getCardWidth() / 10F :
                                        (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                                var renderX = card.isToken() ?
                                        lastTokenX.updateAndGet(x -> x + deltaX) :
                                        lastNormalX.updateAndGet(x -> x + deltaX);
                                var renderY = card.isToken() ? this.height / 5F : 0;
                                game.attachedCard(card).ifPresent(sub -> this.cardGeometries.add(new CardGeometry(sub,
                                        new Rectangle2D.Float(renderX, renderY + this.getCardHeight() / 10F, this.getCardWidth(), this.getCardHeight()))));
                                if (!card.isTapped()) {
                                    this.cardGeometries.add(new CardGeometry(card,
                                            new Rectangle2D.Float(renderX, renderY, this.getCardWidth(), this.getCardHeight())));
                                } else {
                                    this.cardGeometries.add(new CardGeometry(card,
                                            new Rectangle2D.Float(renderX, renderY + this.getCardHeight() - this.getCardWidth(), this.getCardHeight(), this.getCardWidth())));
                                }
                                if (!card.isToken()) beforeCardType.set(type);
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
                                        this.height * 4 / 5F,
                                        this.getCardWidth(), this.getCardHeight())));
                        handsCount.getAndIncrement();
                    }
            );
        }
        {
            AtomicReference<Float> lastNormalX = new AtomicReference<>(caX - this.getCardWidth() * 0.8F);
            AtomicReference<Float> lastTokenX = new AtomicReference<>(caX - this.getCardWidth() / 10F);
            AtomicReference<CardKind> beforeCardType = new AtomicReference<>(null);
            alice.field().stream()
                    .filter(card -> !game.isAttachSub(card))
                    .sorted(comparator)
                    .forEach(
                            card -> {
                                var type = card.getType();
                                var deltaX = card.isToken() ?
                                        this.getCardWidth() / 10F :
                                        (beforeCardType.get() == type ? this.getCardWidth() / 10F : this.getCardWidth() * 0.8F);
                                var renderX = card.isToken() ?
                                        lastTokenX.updateAndGet(x -> x + deltaX) :
                                        lastNormalX.updateAndGet(x -> x + deltaX);
                                var renderY = card.isToken() ? this.height * 2 / 5 : this.height * 3 / 5;
                                game.attachedCard(card).ifPresent(sub -> this.cardGeometries.add(new CardGeometry(sub,
                                        new Rectangle2D.Float(renderX, renderY + this.getCardHeight() / 10F, this.getCardWidth(), this.getCardHeight()))));
                                if (!card.isTapped()) {
                                    this.cardGeometries.add(new CardGeometry(card,
                                            new Rectangle2D.Float(renderX, renderY, this.getCardWidth(), this.getCardHeight())));
                                } else {
                                    this.cardGeometries.add(new CardGeometry(card,
                                            new Rectangle2D.Float(renderX, renderY + this.getCardHeight() - this.getCardWidth(), this.getCardHeight(), this.getCardWidth())));
                                }
                                if (!card.isToken()) beforeCardType.set(type);
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
        return this.height / 5F * 0.9F;
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
        var image = card.isToken() ? this.tokenImage : this.cardInfos.get(card.getType()).mappedImage();
        this.image(image, 0, 0, this.getCardWidth(), this.getCardHeight());
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
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
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
            });
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(element.dieType());
                c.setCreateColor(element.createColor());
                c.setCreateType(element.createType());
            });
            card.setPhaseIn(element.phaseIn());
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            fields.add(card);
        });

        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Lhurgoyf);
                c.setCreateColor(CardColor.Green);
                c.setCreateType(CreatureType.Lhurgoyf);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator);
            card.asThatCard(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Rat);
                c.setCreateColor(CardColor.White);
                c.setCreateType(CreatureType.Rat);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            fields.add(card);
        }

        fields.addAll(RealCard.createCards(List.of(
                CardKind.WildEvocation, CardKind.Recycle,
                CardKind.PrivilegedPosition, CardKind.Vigor,
                CardKind.BlazingArchon
        )));

        for (int i = 0; i < 10; i++) {
            fields.add(new Token());
        }

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
