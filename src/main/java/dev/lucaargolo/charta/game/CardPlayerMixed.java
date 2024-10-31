package dev.lucaargolo.charta.game;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardPlayerMixed {

    List<Card> charta_getHand();
    void charta_handUpdated();
    ResourceLocation charta_getTexture();
    CompletableFuture<Card> charta_getPlay(CardGame game);
    void charta_setPlay(CompletableFuture<Card> play);

    CardPlayer charta_getCardPlayer();

}
