package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.impl.solitaire.SolitaireMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record RestoreSolitairePayload() implements CustomPacketPayload {

    public static final Type<RestoreSolitairePayload> TYPE = new Type<>(ChartaMod.id("restore_solitaire"));

    public static final StreamCodec<ByteBuf, RestoreSolitairePayload> STREAM_CODEC = StreamCodec.unit(new RestoreSolitairePayload());

    public static void handleServer(RestoreSolitairePayload payload, ServerPlayer player, Executor executor) {
        executor.execute(() -> {
            if(player.containerMenu instanceof SolitaireMenu solitaireMenu) {
                solitaireMenu.getGame().restore();
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
