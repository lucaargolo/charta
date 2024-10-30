package dev.lucaargolo.charta.game;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardGame {

    List<CardPlayer> getPlayers();

    CardPlayer getCurrentPlayer();

    CardPlayer getNextPlayer();

    void startGame();

    CompletableFuture<Void> runGame();

    boolean canPlayCard(CardPlayer player, Card card);

    @Nullable Card getBestCard(CardPlayer cards);

    boolean isGameOver();

    CardPlayer getWinner();

    CompoundTag toNbt(CompoundTag tag);

    static void dealCards(LinkedList<Card> drawPile, CardPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawPile.pollLast();
            player.getHand().add(card);
        }
    }

}