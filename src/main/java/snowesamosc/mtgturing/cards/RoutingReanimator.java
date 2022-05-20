package snowesamosc.mtgturing.cards;

import kotlin.Pair;
import snowesamosc.mtgturing.CardKind;

import java.util.List;

public class RoutingReanimator extends RealCard {
    private final CreatureType originalDieType = CreatureType.Cleric;
    private final CardColor originalCreateColor = CardColor.Black;
    private final CreatureType originalCreateType = CreatureType.Zombie;
    private CreatureType dieType = CreatureType.Cleric;
    private CardColor createColor = CardColor.Black;
    private CreatureType createType = CreatureType.Zombie;

    public CreatureType getOriginalDieType() {
        return this.originalDieType;
    }

    public void setDieType(CreatureType dieType) {
        this.dieType = dieType;
    }

    public CardColor getOriginalCreateColor() {
        return this.originalCreateColor;
    }

    public void setCreateColor(CardColor createColor) {
        this.createColor = createColor;
    }

    public CreatureType getOriginalCreateType() {
        return this.originalCreateType;
    }

    public void setCreateType(CreatureType createType) {
        this.createType = createType;
    }

    @Override
    public List<Pair<CreatureType, CreatureType>> getReplaceTypes() {
        return List.of(
                new Pair<>(this.originalDieType, this.dieType),
                new Pair<>(this.originalCreateType, this.createType)
        );
    }

    @Override
    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return List.of(new Pair<>(this.originalCreateColor, this.createColor));
    }

    @Override
    public CardKind getType() {
        return CardKind.RotlungReanimator;
    }
}
