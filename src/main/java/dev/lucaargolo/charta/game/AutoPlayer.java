package dev.lucaargolo.charta.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AutoPlayer implements CardPlayer {

    private final List<Card> hand = new ArrayList<>();

    @Override
    public Collection<Card> getHand() {
        return hand;
    }

    @Override
    public CompletableFuture<Card> getPlay(CardGame game) {
        Card card = game.getBestCard(this);
        return CompletableFuture.completedFuture(card);
    }

    @Override
    public void handUpdated() {

    }

}
