package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

import java.util.Optional;

public class SharedTriumph extends RealCard {
    private CreatureType selectedType = null;

    @Override
    public Optional<CreatureType> getSelectedType() {
        return Optional.ofNullable(this.selectedType);
    }

    public void setSelectedType(CreatureType selectedType) {
        this.selectedType = selectedType;
    }

    @Override
    public CardKind getType() {
        return CardKind.SharedTriumph;
    }
}
