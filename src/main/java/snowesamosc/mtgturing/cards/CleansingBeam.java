package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class CleansingBeam extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Red);
    }

    @Override
    public CardKind getType() {
        return CardKind.CleansingBeam;
    }
}
