package snowesamosc.mtgturing.abilityonstack;

import snowesamosc.mtgturing.OnStackObject;
import snowesamosc.mtgturing.Player;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.Optional;

public abstract class AbilityOnStack implements OnStackObject {
    private RealCard source;
    private Player controller;

    public AbilityOnStack(RealCard source, Player controller) {
        this.source = source;
        this.controller = controller;
    }

    @Override
    public RealCard getSource() {
        return this.source;
    }

    @Override
    public Optional<Player> getController() {
        return Optional.of(this.controller);
    }

    @Override
    public abstract void resolve();
}
