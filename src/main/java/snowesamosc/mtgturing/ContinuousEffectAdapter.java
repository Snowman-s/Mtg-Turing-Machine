package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ContinuousEffectAdapter implements ContinuousEffect {
    @Override
    public Map<RealCard, Pair<Integer, Integer>> addPT() {
        return Collections.emptyMap();
    }

    @Override
    public Set<RealCard> getShroudCard() {
        return Collections.emptySet();
    }

    @Override
    public Set<RealCard> getHexproofCard() {
        return Collections.emptySet();
    }

    @Override
    public Set<RealCard> getPhasingCard() {
        return Collections.emptySet();
    }

    @Override
    public Set<RealCard> getNotUntappableOnUntapStep() {
        return Collections.emptySet();
    }

    @Override
    public Set<Pair<Player, RealCard>> getChangeController() {
        return Collections.emptySet();
    }

    @Override
    public Map<RealCard, Set<CardSubType>> addSubType() {
        return Collections.emptyMap();
    }
}
