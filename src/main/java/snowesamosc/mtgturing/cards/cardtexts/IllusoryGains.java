package snowesamosc.mtgturing.cards.cardtexts;

import kotlin.Pair;
import snowesamosc.mtgturing.ContinuousEffect;
import snowesamosc.mtgturing.ContinuousEffectAdapter;
import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class IllusoryGains extends CardText {
    @Override
    public Optional<ContinuousEffect> getStaticEffect() {
        return Optional.of(
                new ContinuousEffectAdapter() {
                    @Override
                    public Set<Pair<Player, RealCard>> getChangeController() {
                        var game = Game.getInstance();
                        var controller = IllusoryGains.this.getOwner().getController();
                        var attachedCard = game.attachedCard(IllusoryGains.this.getOwner());

                        if (controller.isEmpty() || attachedCard.isEmpty()) {
                            return Collections.emptySet();
                        }

                        return Collections.singleton(new Pair<>(controller.get(), attachedCard.get()));
                    }
                }
        );
    }
}
