package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class SoulSnuffers extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Black);
    }

    @Override
    public CardKind getType() {
        return CardKind.SoulSnuffers;
    }
}
