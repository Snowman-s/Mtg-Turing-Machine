package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Map;

public interface ContinuousEffect {
    Map<RealCard, Pair<Integer, Integer>> addPT();
}
