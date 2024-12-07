package dev.lucaargolo.charta.mixin;

import com.mojang.authlib.GameProfile;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LivingEntityMixed {

    @Unique
    private final GameSlot charta_hand = new GameSlot();
    @Unique
    private CompletableFuture<CardPlay> charta_play = new CompletableFuture<>();
    @Unique
    private final CardPlayer charta_cardPlayer = new CardPlayer() {

        @Override
        public GameSlot getHand() {
            return charta_hand;
        }

        @Override
        public void play(CardPlay play) {
            charta_play.complete(play);
        }

        @Override
        public void afterPlay(Consumer<CardPlay> consumer) {
            charta_play.thenAccept(play -> {
                try{
                    consumer.accept(play);
                }catch (Exception e) {
                    Charta.LOGGER.error("Error while handling {}'s Card Play. ", this.getName().getString(), e);
                }
            });
        }

        @Override
        public void resetPlay() {
            charta_play = new CompletableFuture<>();
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
        public LivingEntity getEntity() {
            return ServerPlayerMixin.this;
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

