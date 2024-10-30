package dev.lucaargolo.charta.game;

import net.minecraft.nbt.CompoundTag;

public interface CardGame {

    void startGame();

    void dealCards();

    boolean playCard(int playerIndex, Card card);

    boolean isGameOver();

    int getWinner();

    void displayGameState();

    CompoundTag toNbt(CompoundTag tag);

}