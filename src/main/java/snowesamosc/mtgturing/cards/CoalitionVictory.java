package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class CoalitionVictory extends RealCard {
    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.White, CardColor.Blue, CardColor.Black, CardColor.Red, CardColor.Green);
    }

    @Override
    public CardKind getType() {
        return CardKind.CoalitionVictory;
    }
}
