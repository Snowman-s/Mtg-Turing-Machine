package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.List;
import java.util.Optional;

public class SteelyResolve extends RealCard {
    private CreatureType selectedType = null;

    @Override
    public Optional<CreatureType> getSelectedType() {
        return Optional.ofNullable(this.selectedType);
    }

    public void setSelectedType(CreatureType selectedType) {
        this.selectedType = selectedType;
    }

    @Override
    public List<CardColor> getOriginalColors() {
        return List.of(CardColor.Green);
    }

    @Override
    public CardKind getType() {
        return CardKind.SteelyResolve;
    }
}
