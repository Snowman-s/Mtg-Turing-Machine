package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.RealCard;

import java.util.Optional;

public interface OnStackObject {
    RealCard getSource();

    Optional<Player> getController();

    void resolve();

    boolean toGYAfterResolved();
}
