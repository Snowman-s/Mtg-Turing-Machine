package snowesamosc.mtgturing;

import java.util.function.Consumer;

public class Game {
    private static final Game instance = new Game();
    private Player bob;
    private Player alice;
    private Player turnPlayer;
    private Phase phase;
    private Consumer<String> logger;

    private Game() {

    }

    public static Game getInstance() {
        return instance;
    }

    public void init(Player bob, Player alice, Consumer<String> logger) {
        this.bob = bob;
        this.alice = alice;
        this.logger = logger;

        this.turnPlayer = alice;
        this.phase = Phase.Untap;
    }

    public Player getBob() {
        return this.bob;
    }

    public Player getAlice() {
        return this.alice;
    }

    public Player getTurnPlayer() {
        return this.turnPlayer;
    }

    public Phase getPhase() {
        return this.phase;
    }
}