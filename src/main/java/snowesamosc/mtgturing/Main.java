package snowesamosc.mtgturing;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.MouseEvent;
import snowesamosc.mtgturing.cards.*;
import snowesamosc.mtgturing.cards.cardtexts.*;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Main extends PApplet {
    private final AtomicBoolean loadEnded = new AtomicBoolean(false);
    private List<CreatureType> tape;
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
            var count = new CountDownLatch(5);
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
            new Thread(() -> {
                this.tape = TapeLoader.loadTape();
                System.out.println("Tape data was loaded.");
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
            var createCardMap = new EnumMap<CardKind, RealCard.CardCreateData>(CardKind.class);
            this.cardInfos.forEach((key1, value) ->
                    createCardMap.put(key1,
                            new RealCard.CardCreateData(value.colors(), value.types(), value.creatureTypes(),
                                    value.power(), value.toughness()
                            )
                    )
            );

            game.init(
                    this.createBob(attachList, createCardMap),
                    this.createAlice(attachList, createCardMap),
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

            this.renderCard(this.selectedCard, 0, 0,
                    this.getOpPanelWidth(), this.getOpPanelWidth() * this.getCardAspectRatio());

            String name, text;
            {
                if (this.selectedCard.isToken()) {
                    name = "Token";
                    text = "";
                } else {
                    var cardInfo = this.cardInfos.get(this.selectedCard.getKind());
                    name = cardInfo.cardName();
                    text = cardInfo.cardText();
                }
            }

            this.fill(255);
            this.textFont(this.cardTextFont);
            {
                var offsetY = new AtomicReference<>(this.getOpPanelWidth() * this.getCardAspectRatio() + 15);
                this.text(name, 0, offsetY.get());
                offsetY.set(offsetY.get() + 15);

                this.textSize(13);
                {
                    this.pushStyle();
                    var colorAndTypeText = "";
                    if (!this.selectedCard.getColors().isEmpty()) {
                        colorAndTypeText = this.selectedCard.getColors().stream()
                                .map(prop::translate)
                                .collect(Collectors.joining()) + " - ";
                    }
                    colorAndTypeText += this.selectedCard.getCardTypes().stream()
                            .map(prop::translate)
                            .collect(Collectors.joining());
                    this.text(colorAndTypeText, 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                    this.popStyle();
                }

                if (!this.selectedCard.getCreatureTypes().isEmpty()) {
                    this.text(this.selectedCard.getCreatureTypes().stream()
                            .map(prop::translate)
                            .collect(Collectors.joining(" ")), 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                }

                this.selectedCard.getText().getReplaceColors().stream()
                        .map(replaceColor -> "(" + prop.translate(replaceColor.component1()) +
                                " → " + prop.translate(replaceColor.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );
                this.selectedCard.getText().getReplaceTypes().stream()
                        .map(replaceType -> "(" + prop.translate(replaceType.component1()) +
                                " → " + prop.translate(replaceType.component2()) + ")")
                        .forEach(
                                t -> {
                                    this.text(t, 0, offsetY.get());
                                    offsetY.set(offsetY.get() + 15);
                                }
                        );
                this.selectedCard.getText().getSelectedType().ifPresent(
                        type -> {
                            this.text("★" + prop.translate(type), 0, offsetY.get());
                            offsetY.set(offsetY.get() + 15);
                        }
                );
                if (this.selectedCard.getCardTypes().contains(CardType.Creature)) {
                    this.text(this.selectedCard.getPower() + "/" + this.selectedCard.getToughness(), 0, offsetY.get());
                    offsetY.set(offsetY.get() + 15);
                }

                this.textSize(15);
                this.text(text, 0, offsetY.get(), this.getOpPanelWidth(), this.height - offsetY.get());
            }
            this.popStyle();
        }
        this.popStyle();

        this.setCardGeometries();

        this.pushStyle();
        this.cardGeometries.forEach(i -> this.renderCard(i.card, i.geometry.x, i.geometry.y, this.getCardWidth(), this.getCardHeight()));
        this.popStyle();
    }

    private void setCardGeometries() {
        var game = Game.getInstance();
        var bob = game.getBob();
        var alice = game.getAlice();

        Comparator<RealCard> comparator = (a, b) ->
                Comparator.nullsLast(Comparator.comparing((CardKind t) -> t))
                        .compare(a.getKind(), b.getKind());

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
                                var type = card.getKind();
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
                                var type = card.getKind();
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

    private void renderCard(RealCard card, float x, float y, float width, float height) {
        this.pushMatrix();
        this.translate(x, y);
        if (card.isTapped()) {
            this.translate(height, 0);
            this.rotate(PI / 2);
        }
        if (!card.isPhaseIn()) {
            this.tint(100, 100, 100);
        }
        var image = card.isToken() ? this.tokenImage : this.cardInfos.get(card.getKind()).mappedImage();
        this.image(image, 0, 0, width, height);
        var counterNum = card.getPlusOrMinus1CounterNum();
        if (counterNum != 0) {
            this.pushStyle();
            var counterCircleDiameter = width / 3;
            if (counterNum > 0) this.fill(0, 0, 255);
            else this.fill(255, 0, 0);
            this.ellipse(counterCircleDiameter / 2, counterCircleDiameter / 2, counterCircleDiameter, counterCircleDiameter);
            this.textSize(counterCircleDiameter * 0.6F);
            this.fill(255);
            this.textAlign(CENTER, CENTER);
            var counterStr = (counterNum > 0 ? "+" : "-") + abs(counterNum);
            this.text(counterStr, 0, 0, counterCircleDiameter, counterCircleDiameter);
            this.popStyle();
        }
        this.noTint();
        this.popMatrix();
    }

    private Player createAlice(List<AttachInfo> attachList, EnumMap<CardKind, RealCard.CardCreateData> map) {
        var fields = new ArrayList<RealCard>();
        attachList.forEach(attach -> {
            var card = RealCard.createCard(CardKind.CloakOfInvisibility, map);
            attach.setSub(card);
            card.setPhaseIn(attach.getMain().isPhaseIn());
            fields.add(card);
        });
        fields.add(RealCard.createCard(CardKind.WheelOfSunAndMoon, map));
        {
            var illusory = RealCard.createCard(CardKind.IllusoryGains, map);
            var token = new CreatureToken(CardColor.Green, CreatureType.Cephalid, 3, 3);
            var attach = new AttachInfo(token);
            attach.setSub(illusory);
            attachList.add(attach);
            fields.add(illusory);
            fields.add(token);
        }
        {
            RealCard card = RealCard.createCard(CardKind.SteelyResolve, map);
            card.asThatCardText(SteelyResolve.class, c -> c.setSelectedType(CreatureType.AssemblyWorker));
            fields.add(card);
        }
        for (int i = 0; i < 2; i++) {
            RealCard card = RealCard.createCard(CardKind.DreadOfNight, map);
            card.asThatCardText(DreadOfNight.class, c -> c.setMinusColor(CardColor.Black));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.FungusSliver, map);
            card.asThatCardText(FungusSliver.class, c -> c.setPlusType(CreatureType.Incarnation));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator, map);
            card.asThatCardText(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Lhurgoyf);
                c.setCreateColor(CardColor.Black);
                c.setCreateType(CreatureType.Cephalid);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            card.putPlusOrMinus1Counter(3);
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.SharedTriumph, map);
            card.asThatCardText(SharedTriumph.class, c -> c.setSelectedType(CreatureType.Lhurgoyf));
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator, map);
            card.asThatCardText(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Rat);
                c.setCreateColor(CardColor.Black);
                c.setCreateType(CreatureType.Cephalid);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            card.putPlusOrMinus1Counter(3);
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.SharedTriumph, map);
            card.asThatCardText(SharedTriumph.class, c -> c.setSelectedType(CreatureType.Rat));
            fields.add(card);
        }

        fields.addAll(RealCard.createCards(List.of(
                CardKind.Vigor, CardKind.MesmericOrb, CardKind.PrismaticOmen,
                CardKind.Choke, CardKind.BlazingArchon), map));
        {
            var c = RealCard.createCard(CardKind.AncientTomb, map);
            c.tap();
            fields.add(c);
        }

        return new Player(RealCard.createCards(List.of(
                CardKind.CleansingBeam,
                CardKind.CoalitionVictory,
                CardKind.SoulSnuffers
        ), map),
                RealCard.createCards(List.of(
                        CardKind.Infest
                ), map),
                fields);
    }

    private Player createBob(List<AttachInfo> attachRequire, EnumMap<CardKind, RealCard.CardCreateData> map) {
        var fields = new ArrayList<RealCard>();

        this.controllerInfo.forEach(element -> {
            var card = RealCard.createCard(element.kind(), map);
            attachRequire.add(new AttachInfo(card));
            card.asThatCardText(XathridNecromancer.class, c -> {
                c.setDieType(element.dieType());
                c.setCreateColor(element.createColor());
                c.setCreateType(element.createType());
            });
            card.asThatCardText(RoutingReanimator.class, c -> {
                c.setDieType(element.dieType());
                c.setCreateColor(element.createColor());
                c.setCreateType(element.createType());
            });
            card.setPhaseIn(element.phaseIn());
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            card.putPlusOrMinus1Counter(3);
            fields.add(card);
        });

        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator, map);
            card.asThatCardText(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Lhurgoyf);
                c.setCreateColor(CardColor.Green);
                c.setCreateType(CreatureType.Lhurgoyf);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            card.putPlusOrMinus1Counter(3);
            fields.add(card);
        }
        {
            RealCard card = RealCard.createCard(CardKind.RotlungReanimator, map);
            card.asThatCardText(RoutingReanimator.class, c -> {
                c.setDieType(CreatureType.Rat);
                c.setCreateColor(CardColor.White);
                c.setCreateType(CreatureType.Rat);
            });
            card.addColors(Set.of(CardColor.Green, CardColor.White, CardColor.Red));
            card.putPlusOrMinus1Counter(3);
            fields.add(card);
        }

        fields.addAll(RealCard.createCards(List.of(
                CardKind.WildEvocation, CardKind.Recycle,
                CardKind.PrivilegedPosition, CardKind.Vigor,
                CardKind.BlazingArchon
        ), map));

        for (int i = 0; i < this.tape.size(); i++) {
            var elm = this.tape.get(i);
            fields.add(new CreatureToken(CardColor.White, elm, 2 + i, 2 + i));
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
