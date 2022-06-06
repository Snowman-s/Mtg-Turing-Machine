package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Infest extends CardText {
    @Override
    public void resolveThisSpell() {
        var theEffect = new ContinuousEffectAdapter() {
            @Override
            public Map<RealCard, Pair<Integer, Integer>> addPT() {
                var ret = new HashMap<RealCard, Pair<Integer, Integer>>();

                Game.getInstance().getFieldCardsExceptPhaseOut().stream()
                        .filter(card -> card.getCardTypes().contains(CardType.Creature))
                        .forEach(card -> ret.put(card, new Pair<>(-2, -2)));

                return ret;
            }
        };

        Game.getInstance().addUntilTurnEndEffect(Collections.singleton(theEffect));
    }
}

