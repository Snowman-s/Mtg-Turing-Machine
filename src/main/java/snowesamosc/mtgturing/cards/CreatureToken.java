package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.cards.cardtexts.CardText;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class CreatureToken extends RealCard {
    private Collection<CardSubType> originalCardSubTypesForName;

    public CreatureToken(CardColor color, CardSubType cardSubType, int power, int toughness) {
        super(null, Set.of(color), Set.of(CardType.Creature), Set.of(cardSubType),
                new CardText(), power, toughness);

        this.originalCardSubTypesForName = Collections.singleton(cardSubType);
    }

    @Override
    public boolean isToken() {
        return true;
    }

    @Override
    public Optional<CreatureToken> asToken() {
        return Optional.of(this);
    }

    public Collection<CardSubType> originalCardSubTypesForName() {
        return this.originalCardSubTypesForName;
    }
}
