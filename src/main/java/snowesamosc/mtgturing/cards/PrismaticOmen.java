package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class PrismaticOmen extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green);
    }

    @Override
    public CardKind getType() {
        return CardKind.PrismaticOmen;
    }
}
