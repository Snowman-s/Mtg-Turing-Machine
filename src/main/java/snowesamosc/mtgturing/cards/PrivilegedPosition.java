package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class PrivilegedPosition extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green, CardColor.White);
    }

    @Override
    public CardKind getType() {
        return CardKind.PrivilegedPosition;
    }
}
