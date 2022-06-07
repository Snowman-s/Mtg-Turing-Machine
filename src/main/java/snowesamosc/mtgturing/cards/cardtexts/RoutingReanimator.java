package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RoutingReanimator extends CardText {
    private final CardSubType originalDieType = CardSubType.Cleric;
    private final CardColor originalCreateColor = CardColor.Black;
    private final CardSubType originalCreateType = CardSubType.Zombie;
    private CardSubType dieType = CardSubType.Cleric;
    private CardColor createColor = CardColor.Black;
    private CardSubType createType = CardSubType.Zombie;

    public CardSubType getOriginalDieType() {
        return this.originalDieType;
    }

    public void setDieType(CardSubType dieType) {
        this.dieType = dieType;
    }

    public CardColor getOriginalCreateColor() {
        return this.originalCreateColor;
    }

    public void setCreateColor(CardColor createColor) {
        this.createColor = createColor;
    }

    public CardSubType getOriginalCreateType() {
        return this.originalCreateType;
    }

    public void setCreateType(CardSubType createType) {
        this.createType = createType;
    }

    @Override
    public List<Pair<CardSubType, CardSubType>> getReplaceTypes() {
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
    public Set<AbilityOnStack> onDeadCard(Collection<? extends RealCard> deathCards) {
        return super.onDeadCard(deathCards);
    }
}
