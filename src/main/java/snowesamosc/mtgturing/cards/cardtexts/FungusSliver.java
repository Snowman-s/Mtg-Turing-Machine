package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CreatureType;

import java.util.List;

public class FungusSliver extends CardText {
    private final CreatureType originalPlusType = CreatureType.Sliver;
    private CreatureType plusType = CreatureType.Sliver;

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
}
