package snowesamosc.mtgturing;

import kotlin.Pair;
import snowesamosc.mtgturing.abilityonstack.AbilityOnStack;
import snowesamosc.mtgturing.cards.CardSubType;
import snowesamosc.mtgturing.cards.CardType;
import snowesamosc.mtgturing.cards.CreatureToken;
import snowesamosc.mtgturing.cards.RealCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
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

    private final Set<Pair<Player, RealCard>> changeControllerFromEffect = new HashSet<>();

    private final Set<BiConsumer<RealCard, Player>> replaceCardToGY = new HashSet<>();

    private final Set<Function<Player, Optional<Runnable>>> replaceDrawStep = new HashSet<>();

    private final List<Attach> attachList = new ArrayList<>();
    private final List<Pair<Player, RealCard>> playerAttachList = new ArrayList<>();
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

        this.getFieldCardsExceptPhaseOut()
                .forEach(card -> card.getText().getStaticEffect().ifPresent(effectFromStaticAbility::add));

        //???????????????
        effectFromStaticAbility.add(this.createEffectForPlusOrMinusPTCounter());

        this.ptAddersFromEffect.clear();
        this.subtypeAddersFromEffect.clear();
        this.hexproofFromEffect.clear();
        this.shroudFromEffect.clear();
        this.phasingFromEffect.clear();
        this.untappableOnUntapStepFromEffect.clear();
        this.changeControllerFromEffect.clear();
        this.replaceCardToGY.clear();
        this.replaceDrawStep.clear();

        Supplier<Stream<ContinuousEffect>> effects = () ->
                Stream.concat(effectFromStaticAbility.stream(), this.effectUntilTurnEnd.stream());

        effects.get()
                .map(ContinuousEffect::getChangeController)
                .forEach(this.changeControllerFromEffect::addAll);

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

        effects.get()
                .map(ContinuousEffect::getReplaceCardToGY)
                .forEach(this.replaceCardToGY::addAll);

        effects.get()
                .map(ContinuousEffect::getReplaceDrawStep)
                .forEach(this.replaceDrawStep::addAll);
    }

    private void doStateBasedAction() {
        var deathCard = this.getFieldCardsExceptPhaseOut().stream()
                .filter(card -> card.getCardTypes().contains(CardType.Creature))
                .filter(card -> this.getPT(card).component2() <= 0)
                .collect(Collectors.toList());
        this.death(deathCard);

        this.checkStaticAbility();
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

    public void attach(Player player, RealCard sub) {
        this.playerAttachList.removeIf(pair -> pair.component2() == sub);
        this.playerAttachList.add(new Pair<>(player, sub));
    }

    public Optional<RealCard> attachingCard(RealCard main) {
        return this.attachList.stream().filter(attach -> attach.main() == main).findAny().map(Attach::sub);
    }

    public Optional<RealCard> attachedCard(RealCard sub) {
        return this.attachList.stream().filter(attach -> attach.sub() == sub).findAny().map(Attach::main);
    }

    public Optional<Player> attachedPlayer(RealCard sub) {
        return this.playerAttachList.stream().filter(pair -> pair.component2() == sub).findAny().map(Pair::component1);
    }

    public boolean isAttachSub(RealCard sub) {
        return this.attachList.stream().anyMatch(attach -> attach.sub() == sub);
    }

    private void toNextCheckpoint() {
        var newCheckpointIndex = Arrays.stream(GameCheckpoint.values()).toList().indexOf(this.gameCheckpoint) + 1;
        if (newCheckpointIndex > GameCheckpoint.values().length - 1) {
            this.effectUntilTurnEnd.clear();
            this.gameCheckpoint = GameCheckpoint.UntapAndUpkeepStarted;
            this.turnPlayer = this.turnPlayer == this.bob ? this.alice : this.bob;
            this.logger.accept("Now " + this.getPlayerName(this.turnPlayer) + "'s turn.");
        } else {
            this.gameCheckpoint = GameCheckpoint.values()[newCheckpointIndex];
        }
    }

    public void toNext() {
        if (!this.stack.isEmpty()) {
            var resolveCard = this.stack.pop();
            this.logger.accept("Resolve " + this.cardNameGetter.apply(Collections.singleton(resolveCard.getSource())) + ".");
            this.checkStaticAbility();
            resolveCard.resolve();
            this.checkStaticAbility();

            var controller = resolveCard.getController();
            if (controller.isPresent() && resolveCard.toGYAfterResolved()) {
                this.toGY(resolveCard.getSource(), controller.get());
            }
        } else {
            this.toNextCheckpoint();

            //????????????????????????????????????
            replaceCheckpointLoop:
            while (true) {
                switch (this.gameCheckpoint) {
                    case DrawStep -> {
                        var replacer = this.replaceDrawStep.stream()
                                .map(func -> func.apply(this.turnPlayer))
                                .filter(Optional::isPresent)
                                .map(Optional::get).toList();
                        if (replacer.size() > 0) {
                            this.logger.accept(this.getPlayerName(this.turnPlayer) + "'s draw step is replaced.");
                            this.selectByPlayer(replacer, this.turnPlayer).run();
                            this.toNextCheckpoint();
                        } else {
                            break replaceCheckpointLoop;
                        }
                    }
                    default -> {
                        break replaceCheckpointLoop;
                    }
                }
            }

            this.checkStaticAbility();

            switch (this.gameCheckpoint) {
                case UntapAndUpkeepStarted -> this.onUntapAndUpkeepStarted();
                case DrawStep -> this.onDrawStep();
            }
        }

        this.doStateBasedAction();

        //?????????????????????????????????????????????????????????????????????????????????
        var abilityGroup = this.triggeredAbility.stream()
                .collect(Collectors.groupingBy(abilityOnStack -> abilityOnStack.getController().orElse(null)));

        abilityGroup.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> {
                    var player = entry.getKey();

                    var players = this.getPlayers();
                    var i = players.indexOf(player);
                    var turnI = players.indexOf(this.getTurnPlayer());
                    var deltaI = i - turnI;
                    return deltaI >= 0 ? deltaI : i + (players.size() - turnI);
                }))
                .forEach(entry -> {
                    var p = entry.getKey();
                    if (p == null) {
                        this.stack.addAll(this.sortByPlayer(abilityGroup.get(p), p));
                    } else {
                        this.stack.addAll(this.sortByPlayer(abilityGroup.get(p), p));
                    }
                });

        this.triggeredAbility.clear();

        //???????????????????????????
    }

    public void onUntapAndUpkeepStarted() {
        var game = Game.getInstance();
        //???????????????
        this.logger.accept("Untap step started.");
        var phasingCards = game.getFieldsCard(this.getTurnPlayer()).stream()
                .filter(card -> (card.canPhaseInIndependently()) || game.hasPhasing(card))
                .collect(Collectors.toSet());
        if (phasingCards.size() > 0) {
            phasingCards.forEach(RealCard::reversePhasingOnUntapPhase);
            this.checkStaticAbility();
            this.logger.accept(this.cardNameGetter.apply(phasingCards) + " were phase in/out.");
        }

        var untapCard = game.getFieldsCard(this.getTurnPlayer()).stream()
                .filter(card -> !this.untappableOnUntapStepFromEffect.contains(card))
                .filter(RealCard::isTapped)
                .collect(Collectors.toSet());
        this.untap(untapCard);

        //??????????????????
        this.logger.accept("Upkeep step started.");
        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUpkeepStarted())
                .forEach(this::trigger);
    }

    public void onDrawStep() {
        this.turnPlayer.hands().add(this.turnPlayer.deck().remove(0));
        this.checkStaticAbility();
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

    public List<RealCard> getFieldsCard(Player player) {
        if (!this.getPlayers().contains(player)) return Collections.emptyList();

        var playersCard = new ArrayList<>(player.field());

        for (Pair<Player, RealCard> pair : this.changeControllerFromEffect) {
            if (pair.component1() == player) playersCard.add(pair.component2());
            else playersCard.remove(pair.component2());
        }

        return playersCard;
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

    public void death(Collection<? extends RealCard> deathCards) {
        if (deathCards.isEmpty()) return;

        this.logger.accept(this.cardNameGetter.apply(deathCards) + " were dead.");

        //????????????????????????????????????????????????????????????????????????
        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onDeadCard(deathCards))
                .forEach(this::trigger);

        deathCards.forEach(
                card -> this.getPlayers().stream().filter(player -> card.getController().stream().anyMatch(p -> p == player))
                        .findAny()
                        .ifPresent(player -> player.field().remove(card))
        );


        this.checkStaticAbility();
    }

    public void untap(Collection<? extends RealCard> cards) {
        if (cards.isEmpty()) return;

        cards.forEach(RealCard::untap);
        this.checkStaticAbility();
        this.logger.accept(this.cardNameGetter.apply(cards) + " were untapped.");

        this.checkStaticAbility();

        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onUntappedCard(cards))
                .forEach(this::trigger);
    }

    public void createToken(Collection<Pair<Player, ? extends CreatureToken>> tokens) {
        this.logger.accept(this.cardNameGetter.apply(tokens.stream()
                        .map(pair -> pair.component2())
                        .collect(Collectors.toList())
                ) + " were created."
        );

        tokens.forEach(
                tokenPair -> {
                    if (!this.getPlayers().contains(tokenPair.component1())) return;

                    tokenPair.component1().field().add(tokenPair.component2());
                }
        );

        this.checkStaticAbility();

        this.getFieldCardsExceptPhaseOut().stream()
                .map(card -> card.getText().onPermanentEntered(
                        tokens.stream()
                                .map(pair -> pair.component2())
                                .collect(Collectors.toList())
                ))
                .forEach(this::trigger);
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

        this.checkStaticAbility();
    }

    public void castSpell(Player controller, RealCard card) {
        this.logger.accept(this.getPlayerName(controller) + " casted " + this.cardNameGetter.apply(Collections.singleton(card)) + ".");
        this.stack.push(new CastedSpell(controller, card));
    }

    public void toGY(RealCard card, Player player) {
        if (this.replaceCardToGY.isEmpty()) throw new RuntimeException("Cannot send cards to non-existing GY!");

        var replacer = this.selectByPlayer(this.replaceCardToGY, player);

        this.logger.accept(this.cardNameGetter.apply(Collections.singleton(card)) +
                " was going to be go to GY, but the process is replaced.");
        replacer.accept(card, player);
    }

    public <T> T selectByPlayer(Collection<? extends T> collection, Player player) {
        if (collection.size() == 0) {
            throw new RuntimeException("Cannot chose from empty collection.");
        } else if (collection.size() > 1) {
            throw new RuntimeException(this.getPlayerName(player) + " is so cheap robot that it cannot chose from "
                    + collection.size() + "-length collection.");
        }

        var ret = collection.stream().findFirst();
        return ret.get();
    }

    public <T> List<T> sortByPlayer(Collection<? extends T> collection, Player player) {
        if (collection.size() > 1) {
            throw new RuntimeException(this.getPlayerName(player) + " is so cheap robot that it cannot sort "
                    + collection.size() + "-length collection.");
        }

        return List.copyOf(collection);
    }

    private enum GameCheckpoint {
        UntapAndUpkeepStarted,
        DrawStep,
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

        @Override
        public boolean toGYAfterResolved() {
            return this.card.toGYAfterResolved();
        }
    }
}