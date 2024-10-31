package dev.lucaargolo.charta.game;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CrazyEightsGame implements CardGame {

    private final Map<CardPlayer, List<Card>> censoredHands = new HashMap<>();

    private final List<Card> deck;
    private final List<CardPlayer> players;

    private final LinkedList<Card> playPile;
    private final LinkedList<Card> drawPile;

    private CardPlayer current;
    private CardPlayer winner;
    private boolean isGameOver;

    public int drawsLeft = 3;

    public CrazyEightsGame(List<CardPlayer> players) {
        this.players = players;

        ImmutableList.Builder<Card> deckBuilder = new ImmutableList.Builder<>();
        for (Card.Suit suit : Card.Suit.values()) {
            if(suit != Card.Suit.BLANK) {
                for (Card.Rank rank : Card.Rank.values()) {
                    if (rank != Card.Rank.BLANK && rank != Card.Rank.JOKER) {
                        deckBuilder.add(new Card(suit, rank, true));
                    }
                }
            }
        }
        this.deck = deckBuilder.build();

        this.drawPile = new LinkedList<>();
        this.playPile = new LinkedList<>();
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
        return current;
    }

    @Override
    public CardPlayer getNextPlayer() {
        if(current == null) {
            return getPlayers().getFirst();
        }else{
            int indexOf = getPlayers().indexOf(current);
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
        last.flip();
        playPile.addLast(last);

        current = players.getFirst();

        winner = null;
        isGameOver = false;

        System.out.println("Game started");
        System.out.println("Its Player "+getPlayers().indexOf(current)+"'s turn");
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
                System.out.println("Shuffling");
            }else{
                endGame();
            }
        }

        current.getPlay(this).thenAccept(card -> {
            if(card == null) {
                if(drawsLeft > 0) {
                    drawsLeft--;
                    System.out.println("Player "+getPlayers().indexOf(current)+" drawed ("+drawsLeft+" draws left)");
                    //TODO: This is a hack, all players should be able to draw by themselves.
                    if(current instanceof AutoPlayer) {
                        CardGame.dealCards(drawPile, current, getCensoredHand(current), 1);
                        current.handUpdated();
                    }
                    runGame();
                }else{
                    current = getNextPlayer();
                    System.out.println("Its Player "+getPlayers().indexOf(current)+"'s turn");
                    drawsLeft = 3;
                    runGame();
                }
            }else if(canPlayCard(current, card)) {
                System.out.println("Player "+getPlayers().indexOf(current)+" played a "+card.getRank()+" of "+card.getSuit());
                if(current.getHand().remove(card)) {
                    getCensoredHand(current).removeLast();
                    playPile.addLast(card);
                }
                current.handUpdated();
                if(current.getHand().isEmpty()) {
                    endGame();
                }else {
                    current = getNextPlayer();
                    System.out.println("Its Player " + getPlayers().indexOf(current) + "'s turn");
                    drawsLeft = 3;
                    runGame();
                }
            }
        });
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        assert lastCard != null;
        return card.getRank() == lastCard.getRank() || card.getSuit() == lastCard.getSuit();
    }

    @Nullable
    @Override
    public Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
    }

    public void endGame() {
        if(current.getHand().isEmpty()) {
            System.out.println(current +" won the game");
            winner = current;
        }
        isGameOver = true;
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    @Override
    public CardPlayer getWinner() {
        return winner;
    }

    @Override
    public CompoundTag toNbt(CompoundTag tag) {
        // Implementation of saving game state to NBT goes here
        return null;
    }



}
