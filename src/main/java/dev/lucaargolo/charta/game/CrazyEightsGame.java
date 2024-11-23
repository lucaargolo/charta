package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CrazyEightsGame implements CardGame<CrazyEightsGame> {

    private final Map<CardPlayer, TransparentLinkedList<Card>> censoredHands = new HashMap<>();

    private final TransparentLinkedList<Card> playPile = new TransparentLinkedList<>();
    private final TransparentLinkedList<Card> drawPile = new TransparentLinkedList<>();
    private final List<GameSlot> gameSlots = new ArrayList<>();

    private final List<Runnable> scheduledActions = new ArrayList<>();
    private boolean isGameReady;

    private final List<CardPlayer> players;
    private final List<Card> deck;

    private CardPlayer currentPlayer;
    private boolean isGameOver;

    public final LinkedList<Card> suits = new LinkedList<>();
    public boolean isChoosingWild;
    public Suit currentSuit;
    public int drawsLeft = 3;

    public CrazyEightsGame(List<CardPlayer> players, CardDeck deck) {
        this.players = players;

        this.deck = deck.getCards()
            .stream()
            .filter(c -> c.getSuit() != Suit.BLANK && c.getRank() != Rank.BLANK && c.getRank() != Rank.JOKER)
            .map(Card::copy)
            .collect(Collectors.toList());
        this.deck.forEach(Card::flip);

        GameSlot playPileSlot = new GameSlot(playPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        GameSlot drawPileSlot = new GameSlot(drawPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        gameSlots.add(playPileSlot);
        gameSlots.add(drawPileSlot);
    }

    @Override
    public AbstractCardMenu<CrazyEightsGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new CrazyEightsMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
    }

    @Override
    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

    @Override
    public List<Card> getValidDeck() {
        List<Card> necessaryCards = new ArrayList<>();
        for(Suit suit : Suit.values()) {
            for(Rank rank : Rank.values()) {
                if(suit != Suit.BLANK && rank != Rank.BLANK && rank != Rank.JOKER) {
                    necessaryCards.add(new Card(suit, rank));
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

    @Override
    public CardPlayer getNextPlayer() {
        if(currentPlayer == null) {
            return getPlayers().getFirst();
        }else{
            int indexOf = getPlayers().indexOf(currentPlayer);
            return getPlayers().get((indexOf + 1) % players.size());
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

        for(int i = 0; i < 5; i++) {
            for (CardPlayer player : players) {
                scheduledActions.add(() -> {
                    player.playSound(ModSounds.CARD_DRAW.get());
                    CardGame.dealCards(drawPile, player, getCensoredHand(player), 1);
                });
                scheduledActions.add(() -> {});
            }
        }

        Card last = drawPile.pollLast();
        while (last != null && last.getRank() == Rank.EIGHT) {
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
            }else{
                endGame();
            }
        }

        currentPlayer.getPlay(this).thenAccept(card -> {
            currentPlayer.setPlay(new CompletableFuture<>());
            if(card == null) {
                currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                if(drawsLeft > 0) {
                    drawsLeft--;
                    if(currentPlayer.shouldCompute()) {
                        CardGame.dealCards(drawPile, currentPlayer, getCensoredHand(currentPlayer), 1);
                    }
                    runGame();
                }else{
                    currentPlayer = getNextPlayer();
                    drawsLeft = 3;
                    runGame();
                }
            }else if(canPlayCard(currentPlayer, card)) {
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.getSuit();
                if(isChoosingWild) {
                    playPile.removeLast();
                    isChoosingWild = false;
                }
                if(currentPlayer.shouldCompute() && currentPlayer.getHand().remove(card)) {
                    getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                }
                if(currentPlayer.getHand().isEmpty()) {
                    endGame();
                }else if(card.getRank() == Rank.EIGHT) {
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
                        currentPlayer = getNextPlayer();
                        drawsLeft = 3;
                        runGame();
                    }else{
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(List.of(
                                new Card(Suit.SPADES, Rank.BLANK),
                                new Card(Suit.HEARTS, Rank.BLANK),
                                new Card(Suit.CLUBS, Rank.BLANK),
                                new Card(Suit.DIAMONDS, Rank.BLANK)
                        ));
                        drawsLeft = 0;
                        runGame();
                    }
                } else {
                    currentPlayer = getNextPlayer();
                    drawsLeft = 3;
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
        return isGameReady && lastCard != null && ((isChoosingWild && card.getRank() == Rank.BLANK) || card.getRank() == Rank.EIGHT || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit);
    }

    @Nullable
    @Override
    public Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
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
