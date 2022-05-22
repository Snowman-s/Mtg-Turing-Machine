package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class PrismaticLace extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Blue);
    }

    @Override
    public CardKind getType() {
        return CardKind.PrismaticLace;
    }
}
