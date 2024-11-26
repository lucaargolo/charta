package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.FunMenu;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.GameSlot;
import dev.lucaargolo.charta.utils.TransparentLinkedList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FunGame implements CardGame<FunGame> {

    //Rules
    public static final int STACK_ANY_PLUS2_ON_PLUS2 = 0;
    public static final int STACK_SAME_COLOR_PLUS2_ON_PLUS2 = 1;
    public static final int STACK_PLUS4_ON_PLUS2 = 2;
    public static final int STACK_PLUS4_ON_PLUS4 = 3;
    public static final int STACK_SAME_COLOR_PLUS2_ON_PLUS4 = 4;
    public static final int STACK_ANY_PLUS2_ON_PLUS4 = 5;

    private final Map<CardPlayer, TransparentLinkedList<Card>> censoredHands = new HashMap<>();

    private final TransparentLinkedList<Card> playPile = new TransparentLinkedList<>();
    private final TransparentLinkedList<Card> drawPile = new TransparentLinkedList<>();
    private final List<GameSlot> gameSlots = new ArrayList<>();

    private final List<Runnable> scheduledActions = new ArrayList<>();
    private boolean isGameReady;

    private final List<CardPlayer> players;
    private final List<Card> deck;
    private final CardDeck cardDeck;

    private CardPlayer currentPlayer;
    private boolean isGameOver;

    public final LinkedList<Card> suits = new LinkedList<>();
    public boolean isChoosingWild;
    public Suit currentSuit;

    public boolean canDraw = true;
    public int drawStack = 0;
    public boolean startedDraw = false;
    public boolean reversed = false;
    public int rules;

    public FunGame(List<CardPlayer> players, CardDeck deck) {
        this(players, deck, 0b000111);
    }

    public FunGame(List<CardPlayer> players, CardDeck deck, int rules) {
        this.players = players;

        this.deck = deck.getCards()
            .stream()
            .map(Card::copy)
            .collect(Collectors.toList());
        this.deck.forEach(Card::flip);

        this.cardDeck = deck;
        this.rules = rules;

        GameSlot playPileSlot = new GameSlot(playPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        GameSlot drawPileSlot = new GameSlot(drawPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        gameSlots.add(playPileSlot);
        gameSlots.add(drawPileSlot);
    }

    public boolean isRule(int rule) {
        return (rules & (1 << rule)) != 0;
    }

    @Override
    public AbstractCardMenu<FunGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new FunMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
    }

    @Override
    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

    @Override
    public List<Card> getValidDeck() {
        List<Card> necessaryCards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if(suit != Suit.BLANK) {
                for (Rank rank : Rank.values()) {
                    necessaryCards.add(new Card(suit, rank));
                    if(rank != Rank.BLANK && rank != Rank.JOKER && rank != Rank.TEN) {
                        necessaryCards.add(new Card(suit, rank));
                    }
                }
            }
        }
        return necessaryCards;
    }

    public LinkedList<Card> getPlayPile() {
        return playPile;
    }

    public LinkedList<Card> getDrawPile() {
        return drawPile;
    }

    @Override
    public List<CardPlayer> getPlayers() {
        return players;
    }

    @Override
    public TransparentLinkedList<Card> getCensoredHand(CardPlayer player) {
        return censoredHands.computeIfAbsent(player, p -> {
            TransparentLinkedList<Card> list = new TransparentLinkedList<>();
            p.getHand().stream().map(c -> Card.BLANK).forEach(list::add);
            return list;
        });
    }

    @Override
    public CardPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public void setCurrentPlayer(int index) {
        this.currentPlayer = getPlayers().get(index);
    }

    private CardPlayer getNextPlayer() {
        if(currentPlayer == null) {
            return getPlayers().getFirst();
        }else{
            int indexOf = getPlayers().indexOf(currentPlayer);
            if(reversed) {
                if(indexOf - 1 >= 0) {
                    return getPlayers().get((indexOf - 1) % players.size());
                }else{
                    return getPlayers().getLast();
                }
            }else{
                return getPlayers().get((indexOf + 1) % players.size());
            }
        }
    }

    @Override
    public void startGame() {
        drawPile.clear();
        playPile.clear();

        drawPile.addAll(deck);
        Collections.shuffle(drawPile);

        for (CardPlayer player : players) {
            player.setPlay(new CompletableFuture<>());
            player.getHand().clear();
            getCensoredHand(player).clear();
        }

        for(int i = 0; i < 7; i++) {
            for (CardPlayer player : players) {
                scheduledActions.add(() -> {
                    player.playSound(ModSounds.CARD_DRAW.get());
                    CardGame.dealCards(drawPile, player, getCensoredHand(player), 1);
                });
                scheduledActions.add(() -> {});
            }
        }

        Card last = drawPile.pollLast();
        while (last != null && (last.getRank() == Rank.BLANK || last.getRank() == Rank.JACK && last.getRank() == Rank.QUEEN || last.getRank() == Rank.KING || last.getRank() == Rank.JOKER)) {
            drawPile.add(last);
            Collections.shuffle(drawPile);
            last = drawPile.pollLast();
        }
        assert last != null;
        last.flip();
        Card startingCard = last;
        scheduledActions.add(() -> {
            playPile.addLast(startingCard);
            currentSuit = startingCard.getSuit();
        });

        currentPlayer = players.getFirst();
        isChoosingWild = false;
        isGameReady = false;
        isGameOver = false;

        tablePlay(Component.literal("Game Started!"));
        tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
    }

    @Override
    public void runGame() {
        if(!isGameReady) {
            return;
        }

        if(drawPile.isEmpty()) {
            if(playPile.size() > 1) {
                Card lastCard = playPile.pollLast();
                playPile.forEach(Card::flip);
                drawPile.addAll(playPile);
                Collections.shuffle(drawPile);
                playPile.clear();
                playPile.add(lastCard);
                tablePlay(Component.literal("Piles shuffled!"));
            }else{
                endGame();
            }
        }

        currentPlayer.getPlay(this).thenAccept(card -> {
            currentPlayer.setPlay(new CompletableFuture<>());
            if(card == null) {
                boolean canPlay = this.getBestCard(currentPlayer) != null;
                if(canDraw) {
                    currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                    cardPlay(currentPlayer, Component.literal("Player drew a card."));
                    canPlay = drawStack < 1;
                    startedDraw = startedDraw || !canPlay;
                    drawStack--;
                    canDraw = drawStack > 0;
                    if (currentPlayer.shouldCompute()) {
                        CardGame.dealCards(drawPile, currentPlayer, getCensoredHand(currentPlayer), 1);
                    }
                }
                if(!canDraw && !canPlay) {
                    canDraw = true;
                    drawStack = 0;
                    startedDraw = false;
                    currentPlayer = getNextPlayer();
                    tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
                }
                runGame();
            }else if(canPlayCard(currentPlayer, card)) {
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.getSuit();
                if(isChoosingWild) {
                    cardPlay(currentPlayer, Component.literal("Player chose "+cardDeck.getSuitTranslatableKey(currentSuit)));
                    playPile.removeLast();
                    isChoosingWild = false;
                }else{
                    cardPlay(currentPlayer, Component.literal("Player played a "+cardDeck.getCardTranslatableKey(card)));
                }
                if(currentPlayer.shouldCompute() && currentPlayer.getHand().remove(card)) {
                    getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                }
                if(currentPlayer.getHand().isEmpty()) {
                    endGame();
                }else if(card.getRank() == Rank.BLANK || card.getRank() == Rank.JOKER) {
                    if(currentPlayer.shouldCompute()) {
                        Map<Suit, Integer> suitCountMap = new HashMap<>();

                        for (Card C : currentPlayer.getHand()) {
                            Suit suit = C.getSuit();
                            suitCountMap.put(suit, suitCountMap.getOrDefault(suit, 0) + 1);
                        }

                        Suit mostFrequentSuit = null;

                        int maxCount = 0;
                        for (Map.Entry<Suit, Integer> entry : suitCountMap.entrySet()) {
                            if (entry.getValue() > maxCount) {
                                mostFrequentSuit = entry.getKey();
                                maxCount = entry.getValue();
                            }
                        }
                        currentSuit = mostFrequentSuit;
                        cardPlay(currentPlayer, Component.literal("Player chose "+cardDeck.getSuitTranslatableKey(currentSuit)));
                        if(card.getRank() == Rank.JOKER) {
                            drawStack += 4;
                        }else{
                            drawStack = 0;
                        }
                        canDraw = true;
                        currentPlayer = getNextPlayer();
                        if(drawStack > 0) {
                            tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn. They need to draw "+drawStack+" cards."));
                        }else{
                            tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
                        }
                        runGame();
                    }else{
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(List.of(
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.HEARTS, Rank.TWO),
                                new Card(Suit.CLUBS, Rank.THREE),
                                new Card(Suit.DIAMONDS, Rank.FOUR)
                        ));
                        canDraw = false;
                        runGame();
                    }
                } else {
                    if(card.getRank() == Rank.JACK) {
                        currentPlayer = getNextPlayer();
                        tablePlay(Component.literal(currentPlayer.getName().getString()+" was skipped."));
                    }else if(card.getRank() == Rank.QUEEN) {
                        reversed = !reversed;
                        tablePlay(Component.literal("Game direction was reversed."));
                    }
                    if(card.getRank() == Rank.KING) {
                        drawStack += 2;
                    }else{
                        drawStack = 0;
                    }
                    canDraw = true;
                    currentPlayer = getNextPlayer();
                    if(drawStack > 0) {
                        tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn. They need to draw "+drawStack+" cards."));
                    }else{
                        tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
                    }
                    runGame();
                }
            }
        });
    }

    @Override
    public void endGame() {
        if(currentPlayer.getHand().isEmpty()) {
            currentPlayer.sendTitle(Component.translatable("charta.message.you_won").withStyle(ChatFormatting.GREEN), Component.translatable("charta.message.congratulations"));
            getPlayers().stream().filter(player -> player != currentPlayer).forEach(player -> {
                player.sendTitle(Component.translatable("charta.message.you_lost").withStyle(ChatFormatting.RED), Component.translatable("charta.message.won_the_match",currentPlayer.getName()));
            });
        }else{
            getPlayers().forEach(player -> {
                currentPlayer.sendTitle(Component.translatable("charta.message.draw").withStyle(ChatFormatting.YELLOW), Component.translatable("charta.message.no_winner"));
                player.sendMessage(Component.translatable("charta.message.match_unable").withStyle(ChatFormatting.GOLD));
            });
        }
        isGameOver = true;
    }

    @Override
    public void tick() {
        CardGame.super.tick();
        if(!isGameReady) {
            if(!scheduledActions.isEmpty()) {
                scheduledActions.removeFirst().run();
            } else {
                isGameReady = true;
                runGame();
            }
        }
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        if(!isGameReady || lastCard == null) {
            return false;
        }
        if(drawStack > 0) {
            if(startedDraw) {
                return false;
            }else {
                boolean isPlus4 = lastCard.getRank() == Rank.JOKER;
                if (isPlus4) {
                    return (isRule(STACK_PLUS4_ON_PLUS4) && card.getRank() == Rank.JOKER) || (isRule(STACK_ANY_PLUS2_ON_PLUS4) && card.getRank() == Rank.KING) || (isRule(STACK_SAME_COLOR_PLUS2_ON_PLUS4) && card.getRank() == Rank.KING && card.getSuit() == currentSuit);
                } else {
                    return (isRule(STACK_PLUS4_ON_PLUS2) && card.getRank() == Rank.JOKER) || (isRule(STACK_ANY_PLUS2_ON_PLUS2) && card.getRank() == Rank.KING) || (isRule(STACK_SAME_COLOR_PLUS2_ON_PLUS2) && card.getRank() == Rank.KING && card.getSuit() == currentSuit);
                }
            }
        }
        return isChoosingWild || card.getRank() == Rank.BLANK || card.getRank() == Rank.JOKER || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit;
    }

    @Override
    public boolean isGameReady() {
        return isGameReady;
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

}
