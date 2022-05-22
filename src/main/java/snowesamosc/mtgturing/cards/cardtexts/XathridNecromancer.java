package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardColor;
import snowesamosc.mtgturing.cards.CreatureType;

import java.util.List;

public class XathridNecromancer extends CardText {
    private final CreatureType originalDieType = CreatureType.Human;
    private final CardColor originalCreateColor = CardColor.Black;
    private final CreatureType originalCreateType = CreatureType.Zombie;
    private CreatureType dieType = CreatureType.Human;
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

}
