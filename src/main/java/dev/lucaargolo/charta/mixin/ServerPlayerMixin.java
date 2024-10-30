package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayerMixed;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements CardPlayerMixed {

    @Unique
    private CompletableFuture<Card> charta_play = new CompletableFuture<>();

    @Override
    public CompletableFuture<Card> charta_getPlay(CardGame game) {
        return this.charta_play;
    }

    @Override
    public void charta_setPlay(CompletableFuture<Card> play) {
        this.charta_play = play;
    }

}
