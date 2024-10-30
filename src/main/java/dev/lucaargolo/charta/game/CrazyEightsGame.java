package dev.lucaargolo.charta.game;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CrazyEightsGame implements CardGame {

    private final List<Card> deck;
    private final List<CardPlayer> players;
    private final LinkedList<Card> playPile;
    private final LinkedList<Card> drawPile;

    private CardPlayer current;
    private int drawsLeft = 3;
    private CardPlayer winner;
    private boolean isGameOver;

    public CrazyEightsGame(List<CardPlayer> players) {
        this.players = players;

        ImmutableList.Builder<Card> deckBuilder = new ImmutableList.Builder<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                if (rank != Card.Rank.BLANK && rank != Card.Rank.JOKER) {
                    deckBuilder.add(new Card(suit, rank));
                }
            }
        }
        this.deck = deckBuilder.build();

        this.drawPile = new LinkedList<>();
        this.playPile = new LinkedList<>();
    }

    @Override
    public List<CardPlayer> getPlayers() {
        return players;
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
            CardGame.dealCards(drawPile, player, 5);
            player.handUpdated();
        }

        playPile.addLast(drawPile.pollLast());

        current = players.getFirst();
        winner = null;
        isGameOver = false;
    }

    @Override
    public CompletableFuture<Void> runGame() {
        if(drawPile.isEmpty()) {
            if(playPile.size() > 1) {
                Card lastCard = playPile.pollLast();
                drawPile.addAll(playPile);
                Collections.shuffle(drawPile);
                playPile.clear();
                playPile.add(lastCard);
            }else{
                endGame();
            }
        }

        return current.getPlay(this).thenAccept(card -> {
            if(card == null) {
                if(drawsLeft > 0) {
                    drawsLeft--;
                    CardGame.dealCards(drawPile, current, 1);
                    current.handUpdated();
                }else{
                    current = getNextPlayer();
                    drawsLeft = 3;
                }
            }else if(canPlayCard(current, card)){
                current.getHand().remove(card);
                current.handUpdated();
                playPile.addLast(card);
                current = getNextPlayer();
                drawsLeft = 3;
            }
            if(current.getHand().isEmpty()) {
                endGame();
            }
        });
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        assert lastCard != null;
        return getPlayers().contains(player) &&
                player.getHand().contains(card) &&
                (card.rank() == lastCard.rank() || card.suit() == lastCard.suit());
    }

    @Nullable
    @Override
    public Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
    }

    public void endGame() {
        if(current.getHand().isEmpty()) {
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
