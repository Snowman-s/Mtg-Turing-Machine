package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.stream.Collectors;

public class CardText {
    private RealCard owner;

    public List<Pair<CardSubType, CardSubType>> getReplaceTypes() {
        return Collections.emptyList();
    }

    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return Collections.emptyList();
    }

    public Optional<CardSubType> getSelectedType() {
        return Optional.empty();
    }

    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.empty();
    }

    public Set<AbilityOnStack> onUntappedCard(Collection<? extends RealCard> untappedCards) {
        return Collections.emptySet();
    }

    public Set<AbilityOnStack> onUpkeepStarted() {
        return Collections.emptySet();
    }

    public Set<AbilityOnStack> onDeadCard(Collection<? extends RealCard> deathCards) {
        return Collections.emptySet();
    }

    public Set<AbilityOnStack> onPermanentEntered(Collection<? extends RealCard> enteredPermanents) {
        return Collections.emptySet();
    }

    public void resolveThisSpell() {

    }

    protected final Set<AbilityOnStack> createAbilitiesOnStackSet(Collection<? extends Runnable> runnables) {
        var owner = this.getOwner();
        var optionalController = owner.getController();
        return optionalController.map(
                controller -> runnables.stream()
                        .map(this::createAbilityOnStackSet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
        ).orElse(Collections.emptySet());
    }

    protected final Set<AbilityOnStack> createAbilityOnStackSet(Runnable runnable) {
        var owner = this.getOwner();
        var optionalController = owner.getController();
        return optionalController.map(
                controller -> Set.of(
                        (AbilityOnStack) new AbilityOnStack(owner, optionalController.get()) {
                            @Override
                            public void resolve() {
                                runnable.run();
                            }
                        }
                )
        ).orElse(Collections.emptySet());
    }

    public RealCard getOwner() {
        return this.owner;
    }

    public void setOwner(RealCard owner) {
        this.owner = owner;
    }
}
