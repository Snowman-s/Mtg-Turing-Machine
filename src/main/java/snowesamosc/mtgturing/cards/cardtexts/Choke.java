package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Choke extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(new ContinuousEffectAdapter() {
            @Override
            public Set<RealCard> getNotUntappableOnUntapStep() {
                var game = Game.getInstance();
                return game.getFieldCardsExceptPhaseOut()
                        .stream()
                        .filter(card -> game.getSubType(card).contains(CardSubType.Island))
                        .collect(Collectors.toSet());
            }
        });
    }
}
