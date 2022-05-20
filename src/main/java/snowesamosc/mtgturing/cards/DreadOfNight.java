package snowesamosc.mtgturing.cards;

import kotlin.Pair;
import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class DreadOfNight extends RealCard {
    private final CardColor originalMinusColor = CardColor.White;
    private CardColor minusColor = CardColor.White;

    public void setMinusColor(CardColor minusColor) {
        this.minusColor = minusColor;
    }

    public CardColor getOriginalMinusColor() {
        return this.originalMinusColor;
    }

    @Override
    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return List.of(new Pair<>(this.originalMinusColor, this.minusColor));
    }

    @Override
    public CardKind getType() {
        return CardKind.DreadOfNight;
    }
}
