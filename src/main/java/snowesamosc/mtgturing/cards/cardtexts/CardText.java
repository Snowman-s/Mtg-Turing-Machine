package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CreatureType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CardText {
    public List<Pair<CreatureType, CreatureType>> getReplaceTypes() {
        return Collections.emptyList();
    }

    public List<Pair<CardColor, CardColor>> getReplaceColors() {
        return Collections.emptyList();
    }

    public Optional<CreatureType> getSelectedType() {
        return Optional.empty();
    }

    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.empty();
    }
}
