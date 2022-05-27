package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Game {
    private static final Game instance = new Game();
    private final Map<RealCard, List<Pair<Integer, Integer>>> ptAddersFromStaticAbility = new HashMap<>();
    private final Map<RealCard, SortedSet<CardSubType>> subtypeAddersFromStaticAbility = new HashMap<>();
    private final Set<RealCard> hexproofFromStaticAbility = new HashSet<>();
    private final Set<RealCard> shroudFromStaticAbility = new HashSet<>();
    private final Set<RealCard> phasingFromStaticAbility = new HashSet<>();
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

        this.bob.field().stream()
                .filter(RealCard::isPhaseIn)
                .forEach(card -> card.getText().getStaticEffect().ifPresent(effectFromStaticAbility::add));
        this.alice.field().stream()
                .filter(RealCard::isPhaseIn)
                .forEach(card -> card.getText().getStaticEffect().ifPresent(effectFromStaticAbility::add));

        //カウンター
        effectFromStaticAbility.add(this.createEffectForPlusOrMinusPTCounter());

        this.ptAddersFromStaticAbility.clear();
        this.subtypeAddersFromStaticAbility.clear();
        this.hexproofFromStaticAbility.clear();
        this.shroudFromStaticAbility.clear();
        this.phasingFromStaticAbility.clear();

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::addSubType)
                .forEach(
                        map -> map.forEach((key, value) -> {
                            if (!this.subtypeAddersFromStaticAbility.containsKey(key))
                                this.subtypeAddersFromStaticAbility.put(key, new TreeSet<>());
                            this.subtypeAddersFromStaticAbility.get(key).addAll(value);
                        })
                );

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::getHexproofCard)
                .forEach(this.hexproofFromStaticAbility::addAll);

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::getShroudCard)
                .forEach(this.shroudFromStaticAbility::addAll);

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::getPhasingCard)
                .forEach(this.phasingFromStaticAbility::addAll);

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

    public Optional<RealCard> attachingCard(RealCard main) {
        return this.attachList.stream().filter(attach -> attach.main() == main).findAny().map(Attach::sub);
    }

    public Optional<RealCard> attachedCard(RealCard sub) {
        return this.attachList.stream().filter(attach -> attach.sub() == sub).findAny().map(Attach::main);
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

    public Set<CardSubType> getSubType(RealCard card) {
        var subTypeModifierList = this.subtypeAddersFromStaticAbility
                .getOrDefault(card, Collections.emptySortedSet());
        var ret = new HashSet<>(card.getSubTypes());
        ret.addAll(subTypeModifierList);
        return ret;
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

    public ContinuousEffect createEffectForPlusOrMinusPTCounter() {
        return new ContinuousEffectAdapter() {
            @Override
            public Map<RealCard, Pair<Integer, Integer>> addPT() {
                Map<RealCard, Pair<Integer, Integer>> ret = new HashMap<>();

                Game.getInstance().getFieldCardsExceptPhaseOut()
                        .stream()
                        .filter(card -> card.getCardTypes().contains(CardType.Creature))
                        .filter(card -> card.getPlusOrMinus1CounterNum() != 0)
                        .forEach(card -> ret.put(card,
                                new Pair<>(card.getPlusOrMinus1CounterNum(), card.getPlusOrMinus1CounterNum()))
                        );

                return ret;
            }
        };
    }

    public boolean isHexProof(RealCard card) {
        return this.hexproofFromStaticAbility.contains(card);
    }

    public boolean isShroud(RealCard card) {
        return this.shroudFromStaticAbility.contains(card);
    }

    public boolean isPhasing(RealCard card) {
        return this.phasingFromStaticAbility.contains(card);
    }

    public static record Attach(RealCard main, RealCard sub) {
    }
}