package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class WheelOfSunAndMoon extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(
                new ContinuousEffectAdapter() {
                    @Override
                    public Set<BiConsumer<RealCard, Player>> getReplaceCardToGY() {

                        return Collections.singleton(
                                (card, player) -> {
                                    if (Game.getInstance().attachedPlayer(WheelOfSunAndMoon.this.getOwner())
                                            .stream()
                                            .anyMatch(player::equals)) {
                                        player.deck().add(card);
                                    }
                                }
                        );
                    }
                }
        );
    }
}
