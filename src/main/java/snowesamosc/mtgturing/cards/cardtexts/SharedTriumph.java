package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.CreatureType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SharedTriumph extends CardText {
    private CreatureType selectedType = null;

    @Override
    public Optional<CreatureType> getSelectedType() {
        return Optional.ofNullable(this.selectedType);
    }

    public void setSelectedType(CreatureType selectedType) {
        this.selectedType = selectedType;
    }

    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(new ContinuousEffectAdapter() {
            @Override
            public Map<RealCard, Pair<Integer, Integer>> addPT() {
                Map<RealCard, Pair<Integer, Integer>> ret = new HashMap<>();

                Game.getInstance().getFieldCardsExceptPhaseOut()
                        .stream()
                        .filter(card -> card.getCreatureTypes().contains(SharedTriumph.this.selectedType))
                        .filter(card -> card.getCardTypes().contains(CardType.Creature))
                        .forEach(card -> ret.put(card, new Pair<>(1, 1)));

                return ret;
            }
        });
    }
}
