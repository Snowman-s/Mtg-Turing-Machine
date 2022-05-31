package snowesamosc.mtgturing.cards.cardtexts;

import snowesamosc.mtgturing.Game;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WildEvocation extends CardText {
    @Override
    public Set<AbilityOnStack> onUpkeepStarted() {
        return this.createAbilityOnStackSet(() -> {
            var game = Game.getInstance();

            var hands = game.getTurnPlayer().hands();
            if (hands.isEmpty()) return;
            var targetCard = hands.remove(ThreadLocalRandom.current().nextInt(hands.size()));
            game.castSpell(targetCard);
        });
    }
}
