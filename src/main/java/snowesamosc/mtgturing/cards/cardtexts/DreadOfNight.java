package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardColor;

import java.util.List;

public class DreadOfNight extends CardText {
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
}
