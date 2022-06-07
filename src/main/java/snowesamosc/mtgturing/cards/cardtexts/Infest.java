package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Infest extends CardText {
    @Override
    public void resolveThisSpell() {
        var affectedCard = Game.getInstance().getFieldCardsExceptPhaseOut().stream()
                .filter(card -> card.getCardTypes().contains(CardType.Creature))
                .collect(Collectors.toSet());

        var theEffect = new ContinuousEffectAdapter() {
            @Override
            public Map<RealCard, Pair<Integer, Integer>> addPT() {
                var ret = new HashMap<RealCard, Pair<Integer, Integer>>();

                affectedCard.forEach(card -> ret.put(card, new Pair<>(-2, -2)));

                return ret;
            }
        };

        Game.getInstance().addUntilTurnEndEffect(Collections.singleton(theEffect));
    }
}

