package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DreadOfNight extends CardText {
    private final CardColor originalMinusColor = CardColor.White;
    private CardColor minusColor = CardColor.White;

    public void setMinusColor(CardColor minusColor) {
        this.minusColor = minusColor;
    }

    public CardColor getOriginalMinusColor() {
        return this.originalMinusColor;
    }

    @Override
    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return List.of(new Pair<>(this.originalMinusColor, this.minusColor));
    }

    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(new ContinuousEffectAdapter() {
            @Override
            public Map<RealCard, Pair<Integer, Integer>> addPT() {
                Map<RealCard, Pair<Integer, Integer>> ret = new HashMap<>();

                Game.getInstance().getFieldCardsExceptPhaseOut()
                        .stream()
                        .filter(card -> card.getColors().contains(DreadOfNight.this.minusColor))
                        .filter(card -> card.getCardTypes().contains(CardType.Creature))
                        .forEach(card -> ret.put(card, new Pair<>(-1, -1)));

                return ret;
            }
        });
    }
}
