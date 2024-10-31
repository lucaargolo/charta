package dev.lucaargolo.charta.game;

import java.util.concurrent.CompletableFuture;

public interface CardPlayer extends CardHolder {

    CompletableFuture<Card> getPlay(CardGame game);

    void setPlay(CompletableFuture<Card> play);


}
