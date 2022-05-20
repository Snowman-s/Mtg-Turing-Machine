package snowesamosc.mtgturing.cards;

import snowesamosc.mtgturing.CardKind;

public class SharedTriumph extends RealCard {
    private CreatureType selectedType = null;

    public void setSelectedType(CreatureType selectedType) {
        this.selectedType = selectedType;
    }

    @Override
    public CardKind getType() {
        return CardKind.SharedTriumph;
    }
}
