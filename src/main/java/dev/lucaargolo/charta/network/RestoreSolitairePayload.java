package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RestoreSolitairePayload() implements CustomPacketPayload {

    public static final Type<RestoreSolitairePayload> TYPE = new Type<>(Charta.id("restore_solitaire"));

    public static final StreamCodec<ByteBuf, RestoreSolitairePayload> STREAM_CODEC = StreamCodec.unit(new RestoreSolitairePayload());

    public static void handleServer(Player player, RestoreSolitairePayload payload) {
        if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof SolitaireMenu solitaireMenu) {
            solitaireMenu.getGame().restore();
        }
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
