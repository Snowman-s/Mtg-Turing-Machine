package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Game {
    private static final Game instance = new Game();
    private final Map<RealCard, List<Pair<Integer, Integer>>> ptAddersFromStaticAbility = new HashMap<>();
    private final List<Attach> attachList = new ArrayList<>();
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

    private void checkStaticAbility() {
        List<ContinuousEffect> effectFromStaticAbility = new ArrayList<>();

        this.bob.field()
                .forEach(card -> card.getText().getStaticEffect().ifPresent(effectFromStaticAbility::add));
        this.alice.field()
                .forEach(card -> card.getText().getStaticEffect().ifPresent(effectFromStaticAbility::add));

        this.ptAddersFromStaticAbility.clear();

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::addPT)
                .forEach(
                        map -> map.forEach((key, value) -> {
                            if (!this.ptAddersFromStaticAbility.containsKey(key))
                                this.ptAddersFromStaticAbility.put(key, new ArrayList<>());
                            this.ptAddersFromStaticAbility.get(key).add(value);
                        })
                );
    }

    public void init(Player bob, Player alice, Consumer<String> logger) {
        this.bob = bob;
        this.alice = alice;
        this.logger = logger;

        this.turnPlayer = alice;
        this.phase = Phase.Untap;

        this.checkStaticAbility();
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

    public void toNext() {

    }

    public Pair<Integer, Integer> getPT(RealCard card) {
        var ptModifierList = this.ptAddersFromStaticAbility.getOrDefault(card, List.of());

        var p = new AtomicInteger(card.getPower());
        var t = new AtomicInteger(card.getToughness());

        ptModifierList.forEach(ptModifier -> {
            p.addAndGet(ptModifier.component1());
            t.addAndGet(ptModifier.component2());
        });

        return new Pair<>(p.get(), t.get());
    }

    public List<RealCard> getFieldCardsExceptPhaseOut() {
        List<RealCard> ret = new ArrayList<>();

        this.bob.field().stream()
                .filter(RealCard::isPhaseIn)
                .forEach(ret::add);
        this.alice.field().stream()
                .filter(RealCard::isPhaseIn)
                .forEach(ret::add);

        return ret;
    }

    public static record Attach(RealCard main, RealCard sub) {
    }
}