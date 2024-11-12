package dev.lucaargolo.charta.mixin;

import com.mojang.authlib.GameProfile;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.TransparentLinkedList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LivingEntityMixed {

    @Unique
    private final TransparentLinkedList<Card> charta_hand = new TransparentLinkedList<>();
    @Unique
    private CompletableFuture<Card> charta_play = new CompletableFuture<>();
    @Unique
    private final CardPlayer charta_cardPlayer = new CardPlayer() {

        @Override
        public TransparentLinkedList<Card> getHand() {
            return charta_hand;
        }

        @Override
        public CompletableFuture<Card> getPlay(CardGame<?> game) {
            return charta_play;
        }

        @Override
        public void setPlay(CompletableFuture<Card> play) {
            charta_play = play;
        }

        @Override
        public void tick(CardGame<?> game) {

        }

        @Override
        public boolean shouldCompute() {
            return false;
        }

        @Override
        public void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck) {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            game.openScreen(serverPlayer, serverPlayer.serverLevel(), pos, deck);
        }

        @Override
        public void sendMessage(Component message) {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            serverPlayer.displayClientMessage(message, false);
        }

        @Override
        public void sendTitle(Component title, @Nullable Component subtitle) {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            if(subtitle != null) {
                serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            }
            serverPlayer.connection.send(new ClientboundSetTitleTextPacket(title));
        }

        @Override
        public Component getName() {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            return serverPlayer.getDisplayName();
        }

        @Override
        public DyeColor getColor() {
            return DyeColor.WHITE;
        }

        @Override
        public int getId() {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            return serverPlayer.getId();
        }

        @Override
        public BlockPos getPosition() {
            ServerPlayer serverPlayer = (ServerPlayer) (Object) ServerPlayerMixin.this;
            return serverPlayer.blockPosition();
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

