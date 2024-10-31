package dev.lucaargolo.charta.game;

import java.util.concurrent.CompletableFuture;

public interface CardPlayerMixed extends CardPlayer, CardHolderMixed {

    CompletableFuture<Card> charta_getPlay(CardGame game);
    void charta_setPlay(CompletableFuture<Card> play);

    default CompletableFuture<Card> getPlay(CardGame game) {
        return charta_getPlay(game);
    }

    default void setPlay(CompletableFuture<Card> play) {
        charta_setPlay(play);
    }

}
