package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.common.ChartaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record GameStartPayload() implements CustomPacketPayload {

    public static final Type<GameStartPayload> TYPE = new Type<>(ChartaMod.id("game_start"));

    public static StreamCodec<ByteBuf, GameStartPayload> STREAM_CODEC = StreamCodec.unit(new GameStartPayload());

    public static void handleClient(GameStartPayload payload, Executor executor) {
        executor.execute(ChartaModClient.LOCAL_HISTORY::clear);
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
