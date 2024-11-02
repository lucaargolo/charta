package dev.lucaargolo.charta.game;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardGame {

    List<CardPlayer> getPlayers();

    List<Card> getCensoredHand(CardPlayer player);

    CardPlayer getCurrentPlayer();

    CardPlayer getNextPlayer();

    void startGame();

    void runGame();

    boolean canPlayCard(CardPlayer player, Card card);

    @Nullable Card getBestCard(CardPlayer cards);

    boolean isGameOver();

    CardPlayer getWinner();

    CompoundTag toNbt(CompoundTag tag);

    default void tick() {
        getPlayers().forEach(p -> p.tick(this));
    }

    static void dealCards(LinkedList<Card> drawPile, CardPlayer player, List<Card> censoredHand, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawPile.pollLast();
            card.flip();
            player.getHand().add(card);
            censoredHand.add(Card.BLANK);
        }
    }

}