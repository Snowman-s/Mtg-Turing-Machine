package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.CreatureType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SteelyResolve extends CardText {
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
            public Set<RealCard> getShroudCard() {
                return Game.getInstance().getFieldCardsExceptPhaseOut()
                        .stream()
                        .filter(card -> card.getCardTypes().contains(CardType.Creature))
                        .filter(card -> card.getCreatureTypes().contains(SteelyResolve.this.selectedType))
                        .collect(Collectors.toSet());
            }
        });
    }
}
