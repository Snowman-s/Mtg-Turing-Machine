package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Map;

public class ContinuousEffectAdapter implements ContinuousEffect {
    @Override
    public Map<RealCard, Pair<Integer, Integer>> addPT() {
        return Collections.emptyMap();
    }
}
