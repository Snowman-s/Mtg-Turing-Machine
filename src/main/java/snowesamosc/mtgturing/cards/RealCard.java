package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.cardtexts.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RealCard {
    private final Set<CardColor> originalColors;
    private final SortedSet<CardColor> colorsAdded = new TreeSet<>();
    private final Set<CardType> originalCardTypes;
    private final Set<CardSubType> originalCardSubTypes;
    private final SortedSet<CardSubType> cardSubTypesAdded = new TreeSet<>();
    private final CardText cardText;
    private final CardKind cardKind;
    private final int originalPower;
    private final int originalToughness;
    private boolean tapped = false;
    private boolean phaseIn = true;
    private int plus1CounterNum = 0;

    public RealCard(CardKind kind, Set<CardColor> originalColors, Set<CardType> originalCardTypes,
                    Set<CardSubType> originalCardSubTypes, CardText cardText,
                    int power, int toughness) {
        this.cardKind = kind;
        this.originalColors = originalColors;
        this.originalCardTypes = originalCardTypes;
        this.originalCardSubTypes = originalCardSubTypes;
        this.cardText = cardText;
        this.originalPower = power;
        this.originalToughness = toughness;

        cardText.setOwner(this);
    }

    public static RealCard createCard(CardKind kind, EnumMap<CardKind, CardCreateData> map) {
        var text = switch (kind) {
            case RotlungReanimator -> new RoutingReanimator();
            case XathridNecromancer -> new XathridNecromancer();
            case CloakOfInvisibility -> new CloakOfInvisibility();
            case WheelOfSunAndMoon -> new WheelOfSunAndMoon();
            case IllusoryGains -> new IllusoryGains();
            case SteelyResolve -> new SteelyResolve();
            case DreadOfNight -> new DreadOfNight();
            case FungusSliver -> new FungusSliver();
            case SharedTriumph -> new SharedTriumph();
            case WildEvocation -> new WildEvocation();
            case Recycle -> new Recycle();
            case PrivilegedPosition -> new PrivilegedPosition();
            case Vigor -> new Vigor();
            case MesmericOrb -> new MesmericOrb();
            case AncientTomb -> new AncientTomb();
            case PrismaticOmen -> new PrismaticOmen();
            case Choke -> new Choke();
            case BlazingArchon -> new BlazingArchon();
            case CleansingBeam -> new CleansingBeam();
            case OliviaVoldaren -> new OliviaVoldaren();
            case SoulSnuffers -> new SoulSnuffers();
            case PrismaticLace -> new PrismaticLace();
            case CoalitionVictory -> new CoalitionVictory();
            case Infest -> new Infest();
        };

        var elm = map.get(kind);

        return new RealCard(kind, elm.colors(), elm.types(), elm.cardSubTypes(), text, elm.power, elm.toughness);
    }

    public static List<RealCard> createCards(List<CardKind> cardTypes, EnumMap<CardKind, CardCreateData> map) {
        return cardTypes.stream()
                .map(type -> createCard(type, map))
                .collect(Collectors.toList());
    }

    public CardKind getKind() {
        return this.cardKind;
    }

    public Set<CardColor> getOriginalColors() {
        return this.originalColors;
    }

    public Set<CardType> getOriginalCardTypes() {
        return this.originalCardTypes;
    }

    public Set<CardSubType> getOriginalSubTypes() {
        return this.originalCardSubTypes;
    }

    public CardText getText() {
        return this.cardText;
    }

    public void addColors(Set<CardColor> colors) {
        this.colorsAdded.addAll(colors);
    }

    public SortedSet<CardColor> getColors() {
        var tmp = new TreeSet<>(this.getOriginalColors());
        tmp.addAll(this.colorsAdded);
        return tmp;
    }

    public SortedSet<CardType> getCardTypes() {
        return new TreeSet<>(this.getOriginalCardTypes());
    }

    public boolean isToken() {
        return false;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }

    public boolean isTapped() {
        return this.tapped;
    }

    public void reversePhasing() {
        this.phaseIn = !this.phaseIn;
    }

    public boolean isPhaseIn() {
        return this.phaseIn;
    }

    public void setPhaseIn(boolean phaseIn) {
        this.phaseIn = phaseIn;
    }

    public void addSubType(Set<CardSubType> cardSubTypes) {
        this.cardSubTypesAdded.addAll(cardSubTypes);
    }

    public SortedSet<CardSubType> getSubTypes() {
        var tmp = new TreeSet<>(this.getOriginalSubTypes());
        tmp.addAll(this.cardSubTypesAdded);
        return tmp;
    }

    public <T extends CardText> void asThatCardText(Class<T> to, Consumer<T> consumer) {
        if (this.cardText.getClass().isAssignableFrom(to)) {
            consumer.accept(to.cast(this.cardText));
        }
    }

    public int getOriginalPower() {
        return this.originalPower;
    }

    public int getPower() {
        return this.originalPower;
    }

    public int getOriginalToughness() {
        return this.originalToughness;
    }

    public int getToughness() {
        return this.originalToughness;
    }

    public void putPlusOrMinus1Counter(int counterNum) {
        this.plus1CounterNum += counterNum;
    }

    public int getPlusOrMinus1CounterNum() {
        return this.plus1CounterNum;
    }

    public Optional<Player> getController() {
        var g = Game.getInstance();

        if (g.getBob().field().stream().anyMatch(card -> card == this)) return Optional.of(g.getBob());
        if (g.getAlice().field().stream().anyMatch(card -> card == this)) return Optional.of(g.getAlice());

        return Optional.empty();
    }

    public record CardCreateData(Set<CardColor> colors, Set<CardType> types,
                                 Set<CardSubType> cardSubTypes, int power, int toughness) {

    }
}
