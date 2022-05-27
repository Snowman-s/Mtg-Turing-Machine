package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Map;
import java.util.Set;

public interface ContinuousEffect {
    Map<RealCard, Pair<Integer, Integer>> addPT();

    Set<RealCard> getShroudCard();

    Set<RealCard> getHexproofCard();

    Set<RealCard> getPhasingCard();

    Map<RealCard, Set<CardSubType>> addSubType();
}
