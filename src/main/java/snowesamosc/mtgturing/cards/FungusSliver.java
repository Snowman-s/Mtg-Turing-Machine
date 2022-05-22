package snowesamosc.mtgturing.cards;

import kotlin.Pair;
import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class FungusSliver extends RealCard {
    private final CreatureType originalPlusType = CreatureType.Sliver;
    private CreatureType plusType = CreatureType.Sliver;

    public FungusSliver() {
        super(List.of(CreatureType.Sliver));
    }

    public void setPlusType(CreatureType plusType) {
        this.plusType = plusType;
    }

    public CreatureType getOriginalPlusType() {
        return this.originalPlusType;
    }

    @Override
    public List<Pair<CreatureType, CreatureType>> getReplaceTypes() {
        return List.of(new Pair<>(this.originalPlusType, this.plusType));
    }

    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green);
    }

    @Override
    public CardKind getType() {
        return CardKind.FungusSliver;
    }
}
