package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.cards.cardtexts.CardText;

import java.util.Set;

public class Token extends RealCard {
    public Token() {
        super(null, Set.of(), Set.of(), Set.of(), new CardText(), 0, 0);
    }

    @Override
    public boolean isToken() {
        return true;
    }

}
