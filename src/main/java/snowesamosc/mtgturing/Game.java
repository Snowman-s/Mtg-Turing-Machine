package snowesamosc.mtgturing;

import snowesamosc.mtgturing.cards.RealCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Game {
    private static final Game instance = new Game();
    private Player bob;
    private Player alice;
    private Player turnPlayer;
    private Phase phase;
    private Consumer<String> logger;

    private List<Attach> attachList = new ArrayList<>();

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

    public void attach(RealCard main, RealCard sub) {
        this.attachList.removeIf(attach -> attach.sub == sub);
        this.attachList.add(new Attach(main, sub));
    }

    public Optional<RealCard> attachedCard(RealCard main) {
        return this.attachList.stream().filter(attach -> attach.main() == main).findAny().map(Attach::sub);
    }

    public boolean isAttachSub(RealCard sub) {
        return this.attachList.stream().anyMatch(attach -> attach.sub() == sub);
    }

    public static record Attach(RealCard main, RealCard sub) {
    }
}