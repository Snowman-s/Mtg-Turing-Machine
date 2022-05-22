package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class OliviaVoldaren extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Black, CardColor.Red);
    }

    @Override
    public CardKind getType() {
        return CardKind.OliviaVoldaren;
    }
}
