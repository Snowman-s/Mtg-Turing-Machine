package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class MesmericOrb extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of();
    }

    @Override
    public CardKind getType() {
        return CardKind.MesmericOrb;
    }
}
