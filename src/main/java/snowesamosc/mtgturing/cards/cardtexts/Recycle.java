package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Player;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Recycle extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(
                new ContinuousEffectAdapter() {
                    @Override
                    public Set<Function<Player, Optional<Runnable>>> getReplaceDrawStep() {
                        return Collections.singleton(
                                player -> Recycle.this.getOwner().getController().stream()
                                        .filter(player::equals)
                                        .findAny()
                                        .map(controller -> () -> {
                                        })
                        );
                    }
                }
        );
    }
}
