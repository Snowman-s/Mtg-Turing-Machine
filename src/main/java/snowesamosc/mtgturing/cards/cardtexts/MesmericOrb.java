package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MesmericOrb extends CardText {
    @Override
    public Set<AbilityOnStack> onUntappedCard(Collection<? extends RealCard> untappedCards) {
        if (untappedCards.isEmpty()) return Collections.emptySet();

        var playerListMap = untappedCards.stream()
                .map(RealCard::getController)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(
                        player -> player,
                        Collectors.summarizingInt(p -> 1)
                ));

        return this.createAbilityOnStackSet(() ->
                playerListMap.forEach(
                        (player, intSummaryStatistics) ->
                                Game.getInstance().mill(player, (int) intSummaryStatistics.getSum())
                )
        );
    }
}
