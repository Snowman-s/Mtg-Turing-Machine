package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;

import java.util.List;

public class FungusSliver extends CardText {
    private final CardSubType originalPlusType = CardSubType.Sliver;
    private CardSubType plusType = CardSubType.Sliver;

    public void setPlusType(CardSubType plusType) {
        this.plusType = plusType;
    }

    public CardSubType getOriginalPlusType() {
        return this.originalPlusType;
    }

    @Override
    public List<Pair<CardSubType, CardSubType>> getReplaceTypes() {
        return List.of(new Pair<>(this.originalPlusType, this.plusType));
    }
}
