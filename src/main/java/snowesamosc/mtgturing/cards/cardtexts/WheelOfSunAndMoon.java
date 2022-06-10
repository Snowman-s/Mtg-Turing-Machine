package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class WheelOfSunAndMoon extends CardText {
    private Player enchantedPlayer = null;

    public void setEnchantedPlayer(Player enchantedPlayer) {
        this.enchantedPlayer = enchantedPlayer;
    }

    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(
                new ContinuousEffectAdapter() {
                    @Override
                    public Set<Consumer<RealCard>> getReplaceCardToGY() {
                        return Collections.singleton(
                                card -> card.getController()
                                        .ifPresent(player -> player.deck().add(card))
                        );
                    }
                }
        );
    }
}
