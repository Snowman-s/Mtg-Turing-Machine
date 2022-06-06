package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {
    private static final Game instance = new Game();
    private final Map<RealCard, List<Pair<Integer, Integer>>> ptAddersFromEffect = new HashMap<>();
    private final Map<RealCard, SortedSet<CardSubType>> subtypeAddersFromEffect = new HashMap<>();

    private final List<ContinuousEffect> effectUntilTurnEnd = new ArrayList<>();
    private final Set<RealCard> hexproofFromEffect = new HashSet<>();
    private final Set<RealCard> shroudFromEffect = new HashSet<>();
    private final Set<RealCard> phasingFromEffect = new HashSet<>();
    private final Set<RealCard> untappableOnUntapStepFromEffect = new HashSet<>();
    private final List<Attach> attachList = new ArrayList<>();
    private final List<AbilityOnStack> triggeredAbility = new ArrayList<>();
    private final Deque<OnStackObject> stack = new ArrayDeque<>();
    private Player bob;
    private Player alice;
    private Player turnPlayer;
    private GameCheckpoint gameCheckpoint;
    private Consumer<String> logger = ignored -> {
    };
    private Function<Collection<? extends RealCard>, String> cardNameGetter = c -> "???";

    private Game() {

    }

    public static Game getInstance() {
        return instance;
    }

    public List<Player> getPlayers() {
        return List.of(this.bob, this.alice);
    }

    public String getPlayerName(Player player) {
        return player == this.bob ? "Bob" :
                player == this.alice ? "Alice" :
                        "Unknown Player";
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

        this.ptAddersFromEffect.clear();
        this.subtypeAddersFromEffect.clear();
        this.hexproofFromEffect.clear();
        this.shroudFromEffect.clear();
        this.phasingFromEffect.clear();
        this.untappableOnUntapStepFromEffect.clear();

        Supplier<Stream<ContinuousEffect>> effects = () ->
                Stream.concat(effectFromStaticAbility.stream(), this.effectUntilTurnEnd.stream());

        effects.get()
                .map(ContinuousEffect::addSubType)
                .forEach(
                        map -> map.forEach((key, value) -> {
                            if (!this.subtypeAddersFromEffect.containsKey(key))
                                this.subtypeAddersFromEffect.put(key, new TreeSet<>());
                            this.subtypeAddersFromEffect.get(key).addAll(value);
                        })
                );

        effects.get()
                .map(ContinuousEffect::getHexproofCard)
                .forEach(this.hexproofFromEffect::addAll);

        effects.get()
                .map(ContinuousEffect::getShroudCard)
                .forEach(this.shroudFromEffect::addAll);

        effects.get()
                .map(ContinuousEffect::getPhasingCard)
                .forEach(this.phasingFromEffect::addAll);

        effects.get()
                .map(ContinuousEffect::addPT)
                .forEach(
                        map -> map.forEach((key, value) -> {
                            if (!this.ptAddersFromEffect.containsKey(key))
                                this.ptAddersFromEffect.put(key, new ArrayList<>());
                            this.ptAddersFromEffect.get(key).add(value);
                        })
                );

        effects.get()
                .map(ContinuousEffect::getNotUntappableOnUntapStep)
                .forEach(this.untappableOnUntapStepFromEffect::addAll);
    }

    public void init(Player bob, Player alice, Consumer<String> logger, Function<Collection<? extends RealCard>, String> cardNameGetter) {
        this.bob = bob;
        this.alice = alice;
        this.logger = logger;
        this.cardNameGetter = cardNameGetter;

        this.turnPlayer = bob;
        this.gameCheckpoint = GameCheckpoint.End;

        this.triggeredAbility.clear();
        this.stack.clear();

        logger.accept("Game started.");

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
            var resolveCard = this.stack.pop();
            this.logger.accept("Resolve " + this.cardNameGetter.apply(Collections.singleton(resolveCard.getSource())) + ".");
            resolveCard.resolve();
            this.checkStaticAbility();
            return;
        } else {
            var newCheckpointIndex = Arrays.stream(GameCheckpoint.values()).toList().indexOf(this.gameCheckpoint) + 1;
            if (newCheckpointIndex > GameCheckpoint.values().length - 1) {
                this.gameCheckpoint = GameCheckpoint.Untap;
                this.turnPlayer = this.turnPlayer == this.bob ? this.alice : this.bob;
                this.logger.accept("Now " + this.getPlayerName(this.turnPlayer) + "'s turn.");
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
        var phasingCards = this.getTurnPlayer().field().stream()
                .filter(card -> (card.canPhaseInIndependently()) || Game.getInstance().hasPhasing(card))
                .collect(Collectors.toSet());
        if (phasingCards.size() > 0) {
            phasingCards.forEach(RealCard::reversePhasingOnUntapPhase);
            this.checkStaticAbility();
            this.logger.accept(this.cardNameGetter.apply(phasingCards) + " were phase in/out.");
        }
        var untapCard = this.getTurnPlayer().field().stream()
                .filter(card -> !this.untappableOnUntapStepFromEffect.contains(card))
                .filter(RealCard::isTapped)
                .collect(Collectors.toSet());
        if (untapCard.size() > 0) {
            untapCard.forEach(RealCard::untap);
            this.checkStaticAbility();
            this.logger.accept(this.cardNameGetter.apply(untapCard) + " were untapped.");
        }

        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUntappedCard(untapCard))
                .forEach(this::trigger);
    }

    public void onUpkeepStarted() {
        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUpkeepStarted())
                .forEach(this::trigger);
        this.triggeredAbility.stream()
                .sorted(Comparator.comparingInt(p -> p.getController().map(c -> this.getPlayers().indexOf(c)).orElse(-1)))
                .forEach(this.stack::push);
        this.triggeredAbility.clear();
    }

    public Pair<Integer, Integer> getPT(RealCard card) {
        var ptModifierList = this.ptAddersFromEffect.getOrDefault(card, List.of());

        var p = new AtomicInteger(card.getPower());
        var t = new AtomicInteger(card.getToughness());

        ptModifierList.forEach(ptModifier -> {
            p.addAndGet(ptModifier.component1());
            t.addAndGet(ptModifier.component2());
        });

        return new Pair<>(p.get(), t.get());
    }

    public Set<CardSubType> getSubType(RealCard card) {
        var subTypeModifierList = this.subtypeAddersFromEffect
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

    public void addUntilTurnEndEffect(Collection<? extends ContinuousEffect> effects) {
        this.effectUntilTurnEnd.addAll(effects);
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
            this.logger.accept(this.getPlayerName(player) + " milled " + number + " cards.");
        }
        this.checkStaticAbility();
    }

    public boolean hasHexProof(RealCard card) {
        return this.hexproofFromEffect.contains(card);
    }

    public boolean hasShroud(RealCard card) {
        return this.shroudFromEffect.contains(card);
    }

    public boolean hasPhasing(RealCard card) {
        return this.phasingFromEffect.contains(card);
    }

    public List<OnStackObject> getStack() {
        return List.copyOf(this.stack);
    }

    public void trigger(Collection<? extends AbilityOnStack> abilities) {
        abilities.stream()
                .map(AbilityOnStack::getSource)
                .map(source -> this.cardNameGetter.apply(Collections.singleton(source)))
                .distinct()
                .forEach(srcString -> this.logger.accept(srcString + "'s ability was triggered."));
        this.triggeredAbility.addAll(abilities);
    }

    public void castSpell(Player controller, RealCard card) {
        this.logger.accept(this.getPlayerName(controller) + " casted " + this.cardNameGetter.apply(Collections.singleton(card)) + ".");
        this.stack.push(new CastedSpell(controller, card));
    }

    private enum GameCheckpoint {
        Untap,
        UpkeepStarted,
        Draw,
        Main,
        End
    }

    public record Attach(RealCard main, RealCard sub) {
    }

    public record CastedSpell(Player controller, RealCard card) implements OnStackObject {
        @Override
        public RealCard getSource() {
            return this.card;
        }

        @Override
        public Optional<Player> getController() {
            return Optional.of(this.controller);
        }

        @Override
        public void resolve() {
            this.card.resolve();
        }
    }
}