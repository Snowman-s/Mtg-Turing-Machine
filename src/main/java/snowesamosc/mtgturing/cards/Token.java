package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

public class Token extends RealCard {
    @Override
    public boolean isToken() {
        return true;
    }

    @Override
    public CardKind getType() {
        return null;
    }
}
