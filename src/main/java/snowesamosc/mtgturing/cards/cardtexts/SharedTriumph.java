package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.cards.CreatureType;

import java.util.Optional;

public class SharedTriumph extends CardText {
    private CreatureType selectedType = null;

    @Override
    public Optional<CreatureType> getSelectedType() {
        return Optional.ofNullable(this.selectedType);
    }

    public void setSelectedType(CreatureType selectedType) {
        this.selectedType = selectedType;
    }

}
