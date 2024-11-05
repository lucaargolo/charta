package dev.lucaargolo.charta.mixin;

import com.mojang.authlib.GameProfile;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LivingEntityMixed {

    @Unique
    private final List<Card> charta_hand = new ArrayList<>();
    @Unique
    private CompletableFuture<Card> charta_play = new CompletableFuture<>();
    @Unique
    private final CardPlayer charta_cardPlayer = new CardPlayer() {
        @Override
        public CompletableFuture<Card> getPlay(CardGame<?> game) {
            return charta_play;
        }

        @Override
        public void setPlay(CompletableFuture<Card> play) {
            charta_play = play;
        }

        @Override
        public List<Card> getHand() {
            return charta_hand;
        }

        @Override
        public void handUpdated() {
            entityData.set(Charta.ENTITY_HAND, charta_hand);
        }

        @Override
        public void tick(CardGame<?> game) {

        }

        @Override
        public void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck) {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            game.openScreen(serverPlayer, serverPlayer.serverLevel(), pos, deck);
        }

        @Override
        public ResourceLocation getTexture() {
            return null;
        }

        @Override
        public boolean isPreComputed() {
            return true;
        }
    };

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

}

