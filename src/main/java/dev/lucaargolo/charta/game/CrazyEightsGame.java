package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
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

    private final Map<CardPlayer, List<Card>> censoredHands = new HashMap<>();

    private final List<Card> deck;
    private final List<CardPlayer> players;

    private final LinkedList<Card> playPile;
    private final LinkedList<Card> drawPile;

    private CardPlayer currentPlayer;
    private boolean isGameOver;

    public final LinkedList<Card> suits;
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

        this.drawPile = new LinkedList<>();
        this.playPile = new LinkedList<>();
        this.suits = new LinkedList<>();
    }

    @Override
    public AbstractCardMenu<CrazyEightsGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new CrazyEightsMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
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
    public List<Card> getCensoredHand(CardPlayer player) {
        return censoredHands.computeIfAbsent(player, p -> p.getHand().stream().map(c -> Card.BLANK).collect(Collectors.toList()));
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
            CardGame.dealCards(drawPile, player, getCensoredHand(player), 5);
            player.handUpdated();
        }

        Card last = drawPile.pollLast();
        while (last.getRank() == Rank.EIGHT) {
            drawPile.add(last);
            Collections.shuffle(drawPile);
            last = drawPile.pollLast();
        }
        last.flip();
        playPile.addLast(last);
        currentSuit = last.getSuit();

        currentPlayer = players.getFirst();

        isChoosingWild = false;
        isGameOver = false;
    }

    @Override
    public void runGame() {
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
                if(drawsLeft > 0) {
                    drawsLeft--;
                    if(currentPlayer.shouldCompute()) {
                        CardGame.dealCards(drawPile, currentPlayer, getCensoredHand(currentPlayer), 1);
                        currentPlayer.handUpdated();
                    }
                    runGame();
                }else{
                    currentPlayer = getNextPlayer();
                    drawsLeft = 3;
                    runGame();
                }
            }else if(canPlayCard(currentPlayer, card)) {
                currentSuit = card.getSuit();
                if(isChoosingWild) {
                    playPile.removeLast();
                    isChoosingWild = false;
                }
                if(currentPlayer.shouldCompute() && currentPlayer.getHand().remove(card)) {
                    getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                    currentPlayer.handUpdated();
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
            currentPlayer.sendTitle(Component.literal("You won!").withStyle(ChatFormatting.GREEN), Component.literal("Congratulations!"));
            getPlayers().stream().filter(player -> player != currentPlayer).forEach(player -> {
                player.sendTitle(Component.literal("You lost!").withStyle(ChatFormatting.RED), currentPlayer.getName().copy().append(" won the match."));
            });
        }else{
            getPlayers().forEach(player -> {
                player.sendMessage(Component.literal("<Charta> Crazy Eights match was unable to continue, either from lack of players or lack of cards."));
            });
        }
        isGameOver = true;
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        assert lastCard != null;
        return (isChoosingWild && card.getRank() == Rank.BLANK) || card.getRank() == Rank.EIGHT || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit;
    }

    @Nullable
    @Override
    public Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }


}
