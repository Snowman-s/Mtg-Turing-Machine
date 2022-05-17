package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RealCard {
    private final List<CreatureType> availableCreatureTypes;
    private boolean tapped = false;

    public RealCard() {
        this(List.of());
    }

    public RealCard(List<CreatureType> initialCreatureTypes) {
        this.availableCreatureTypes = new ArrayList<>(initialCreatureTypes);
    }

    public static RealCard createCard(CardKind type) {
        return switch (type) {
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
            case Infest-> new Infest();
        };
    }

    public static List<RealCard> createCards(List<CardKind> cardTypes) {
        return cardTypes.stream()
                .map(RealCard::createCard)
                .collect(Collectors.toList());
    }

    public abstract CardKind getType();

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }

    public boolean isTapped() {
        return this.tapped;
    }

    public List<CreatureType> getCreatureTypes() {
        return List.copyOf(this.availableCreatureTypes);
    }
}