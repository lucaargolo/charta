package dev.lucaargolo.charta.game;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AutoPlayer implements CardPlayer {

    private final List<Card> hand = new ArrayList<>();

    @Override
    public List<Card> getHand() {
        return hand;
    }

    @Override
    public CompletableFuture<Card> getPlay(CardGame game) {
        Card card = game.getBestCard(this);
        return CompletableFuture.completedFuture(card);
    }

    @Override
    public void setPlay(CompletableFuture<Card> play) {

    }

    @Override
    public void handUpdated() {

    }

    @Override
    public ResourceLocation getTexture() {
        return null;
    }

}
