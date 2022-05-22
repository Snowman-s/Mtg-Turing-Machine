package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class Vigor extends RealCard {
    public Vigor() {
        super(List.of(CreatureType.Incarnation));
    }

    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green);
    }

    @Override
    public CardKind getType() {
        return CardKind.Vigor;
    }
}
