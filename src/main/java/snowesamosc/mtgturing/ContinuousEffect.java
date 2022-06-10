package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface ContinuousEffect {
    Map<RealCard, Pair<Integer, Integer>> addPT();

    Set<RealCard> getShroudCard();

    Set<RealCard> getHexproofCard();

    Set<RealCard> getPhasingCard();

    Set<RealCard> getNotUntappableOnUntapStep();

    Set<Pair<Player, RealCard>> getChangeController();

    Set<BiConsumer<RealCard, Player>> getReplaceCardToGY();

    Set<Function<Player, Optional<Runnable>>> getReplaceDrawStep();

    Map<RealCard, Set<CardSubType>> addSubType();
}
