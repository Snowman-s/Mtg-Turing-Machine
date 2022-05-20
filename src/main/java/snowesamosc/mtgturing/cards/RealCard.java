package snowesamosc.mtgturing.cards;

import kotlin.Pair;
import snowesamosc.mtgturing.CardKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class RealCard {
    private final List<CreatureType> availableCreatureTypes;
    private boolean tapped = false;
    private boolean phaseIn = true;

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

    public void reversePhasing() {
        this.phaseIn = !this.phaseIn;
    }

    public boolean isPhaseIn() {
        return this.phaseIn;
    }

    public void setPhaseIn(boolean phaseIn) {
        this.phaseIn = phaseIn;
    }

    public List<CreatureType> getCreatureTypes() {
        return List.copyOf(this.availableCreatureTypes);
    }

    public <T extends RealCard> void asThatCard(Class<T> to, Consumer<T> consumer) {
        if (this.getClass().isAssignableFrom(to)) {
            consumer.accept(to.cast(this));
        }
    }

    public List<Pair<CreatureType, CreatureType>> getReplaceTypes() {
        return Collections.emptyList();
    }

    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return Collections.emptyList();
    }
}
