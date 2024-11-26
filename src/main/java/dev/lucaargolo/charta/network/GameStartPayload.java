package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record GameStartPayload() implements CustomPacketPayload {

    public static final Type<GameStartPayload> TYPE = new Type<>(Charta.id("game_start"));

    public static StreamCodec<ByteBuf, GameStartPayload> STREAM_CODEC = StreamCodec.unit(new GameStartPayload());

    public static void handleClient(GameStartPayload payload, IPayloadContext context) {
        context.enqueueWork(GameStartPayload::onGameStart);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onGameStart() {
        ChartaClient.LOCAL_HISTORY.clear();
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
