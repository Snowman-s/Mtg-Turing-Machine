package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Game {
    private static final Game instance = new Game();
    private final Map<RealCard, List<Pair<Integer, Integer>>> ptAddersFromStaticAbility = new HashMap<>();
    private final Map<RealCard, SortedSet<CardSubType>> subtypeAddersFromStaticAbility = new HashMap<>();
    private final Set<RealCard> hexproofFromStaticAbility = new HashSet<>();
    private final Set<RealCard> shroudFromStaticAbility = new HashSet<>();
    private final Set<RealCard> phasingFromStaticAbility = new HashSet<>();
    private final Set<RealCard> untappableOnUntapStepFromStaticAbility = new HashSet<>();
    private final List<Attach> attachList = new ArrayList<>();
    private final List<AbilityOnStack> triggeredAbility = new ArrayList<>();
    private final Deque<OnStackObject> stack = new ArrayDeque<>();
    private Player bob;
    private Player alice;
    private Player turnPlayer;
    private GameCheckpoint gameCheckpoint;
    private Consumer<String> logger;

    private Game() {

    }

    public static Game getInstance() {
        return instance;
    }

    public List<Player> getPlayers() {
        return List.of(this.bob, this.alice);
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
        this.untappableOnUntapStepFromStaticAbility.clear();

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

        effectFromStaticAbility.stream()
                .map(ContinuousEffect::getNotUntappableOnUntapStep)
                .forEach(this.untappableOnUntapStepFromStaticAbility::addAll);
    }

    public void init(Player bob, Player alice, Consumer<String> logger) {
        this.bob = bob;
        this.alice = alice;
        this.logger = logger;

        this.turnPlayer = alice;
        this.gameCheckpoint = GameCheckpoint.Untap;

        this.triggeredAbility.clear();
        this.stack.clear();

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

    public GameCheckpoint getPhase() {
        return this.gameCheckpoint;
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
        if (!this.stack.isEmpty()) {
            this.stack.pop().resolve();
            return;
        } else {
            var newCheckpointIndex = Arrays.stream(GameCheckpoint.values()).toList().indexOf(this.gameCheckpoint) + 1;
            if (newCheckpointIndex > GameCheckpoint.values().length - 1) {
                this.gameCheckpoint = GameCheckpoint.Untap;
            } else {
                this.gameCheckpoint = GameCheckpoint.values()[newCheckpointIndex];
            }
        }

        switch (this.gameCheckpoint) {
            case Untap -> this.onUntap();
            case UpkeepStarted -> this.onUpkeepStarted();
        }
    }

    public void onUntap() {
        this.getTurnPlayer().field().stream()
                .filter(card -> (!card.isPhaseIn()) || Game.getInstance().hasPhasing(card))
                .forEach(RealCard::reversePhasingOnUntapPhase);
        this.checkStaticAbility();
        var untapCard = this.getTurnPlayer().field().stream()
                .filter(card -> !this.untappableOnUntapStepFromStaticAbility.contains(card))
                .filter(RealCard::isTapped)
                .collect(Collectors.toSet());
        untapCard.forEach(RealCard::untap);
        this.checkStaticAbility();

        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUntappedCard(untapCard))
                .forEach(this.triggeredAbility::addAll);
    }

    public void onUpkeepStarted() {
        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUpkeepStarted())
                .forEach(this.triggeredAbility::addAll);
        this.triggeredAbility.stream()
                .sorted(Comparator.comparingInt(p -> p.getController().map(c -> this.getPlayers().indexOf(c)).orElse(-1)))
                .forEach(this.stack::push);
        this.triggeredAbility.clear();
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

    public void mill(Player player, int number) {
        if (number > 0) {
            number = Math.min(number, player.deck().size());
            player.deck().subList(0, number).clear();
            this.logger.accept(number + " card was milled");
        }
        this.checkStaticAbility();
    }

    public boolean hasHexProof(RealCard card) {
        return this.hexproofFromStaticAbility.contains(card);
    }

    public boolean hasShroud(RealCard card) {
        return this.shroudFromStaticAbility.contains(card);
    }

    public boolean hasPhasing(RealCard card) {
        return this.phasingFromStaticAbility.contains(card);
    }

    public List<OnStackObject> getStack() {
        return List.copyOf(this.stack);
    }

    public void castSpell(RealCard card) {
        this.stack.push(card);
    }

    private enum GameCheckpoint {
        Untap,
        UpkeepStarted,
        Draw,
        Main,
        End
    }

    public static record Attach(RealCard main, RealCard sub) {
    }
}