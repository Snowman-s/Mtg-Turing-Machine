package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ContinuousEffect {
    Map<RealCard, Pair<Integer, Integer>> addPT();

    Set<RealCard> getShroudCard();

    Set<RealCard> getHexproofCard();

    Set<RealCard> getPhasingCard();

    Set<RealCard> getNotUntappableOnUntapStep();

    Set<Pair<Player, RealCard>> getChangeController();

    Set<Consumer<RealCard>> getReplaceCardToGY();

    Map<RealCard, Set<CardSubType>> addSubType();
}
