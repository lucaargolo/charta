package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.client.render.screen.HistoryScreen;
import dev.lucaargolo.charta.common.ChartaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record CardPlayPayload(Component playerName, int playerCards, Component play) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CardPlayPayload> TYPE = new CustomPacketPayload.Type<>(ChartaMod.id("card_play"));

    public static StreamCodec<ByteBuf, CardPlayPayload> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        CardPlayPayload::playerName,
        ByteBufCodecs.INT,
        CardPlayPayload::playerCards,
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        CardPlayPayload::play,
        CardPlayPayload::new
    );

    public static void handleClient(CardPlayPayload payload, Executor executor) {
        executor.execute(() -> {
            ChartaModClient.LOCAL_HISTORY.add(ImmutableTriple.of(payload.playerName, payload.playerCards, payload.play));
            Minecraft mc = Minecraft.getInstance();
            if(mc.screen instanceof HistoryScreen screen) {
                screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
