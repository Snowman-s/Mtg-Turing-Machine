package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class BlazingArchon extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.White);
    }

    @Override
    public CardKind getType() {
        return CardKind.BlazingArchon;
    }
}
