package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.cards.cardtexts.CardText;

import java.util.Set;

public class CreatureToken extends RealCard {
    public CreatureToken(CardColor color, CreatureType creatureType, int power, int toughness) {
        super(null, Set.of(color), Set.of(CardType.Creature), Set.of(creatureType),
                new CardText(), power, toughness);
    }

    @Override
    public boolean isToken() {
        return true;
    }

}