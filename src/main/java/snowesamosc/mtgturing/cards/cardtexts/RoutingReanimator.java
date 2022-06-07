package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return this.createAbilitiesOnStackSet(deathCards.stream()
                .filter(card -> card.getCardTypes().contains(CardType.Creature))
                .filter(card -> card.getSubTypes().contains(RoutingReanimator.this.dieType))
                .filter(card -> card.getController().equals(this.getOwner().getController()))
                .map(card -> (Runnable) (() -> {
                            var owner = this.getOwner().getController();
                            if (owner.isEmpty()) return;
                            var token = new CreatureToken(this.createColor, this.createType, 2, 2);
                            Game.getInstance().createToken(Collections.singleton(new Pair<>(owner.get(), token)));
                        })
                )
                .collect(Collectors.toSet())
        );
    }
}
