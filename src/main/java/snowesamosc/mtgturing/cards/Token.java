package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class Token extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of();
    }

    @Override
    public boolean isToken() {
        return true;
    }

    @Override
    public CardKind getType() {
        return null;
    }
}
