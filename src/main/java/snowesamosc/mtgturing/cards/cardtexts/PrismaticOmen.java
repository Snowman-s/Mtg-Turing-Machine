package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;

public class PrismaticOmen extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(
                new ContinuousEffectAdapter() {
                    @Override
                    public Map<RealCard, Set<CardSubType>> addSubType() {
                        Map<RealCard, Set<CardSubType>> ret = new HashMap<>();
                        var game = Game.getInstance();
                        PrismaticOmen.this.getOwner().getController()
                                .map(game::getFieldsCard)
                                .orElse(List.of())
                                .stream()
                                .filter(RealCard::isPhaseIn)
                                .filter(card -> card.getCardTypes().contains(CardType.Land))
                                .forEach(card -> ret.put(card,
                                        Set.of(CardSubType.Plains, CardSubType.Island, CardSubType.Swamp,
                                                CardSubType.Mountain, CardSubType.Forest))
                                );
                        return ret;
                    }
                }
        );
    }
}
