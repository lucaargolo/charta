package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.client.render.screen.ConfirmScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record GameLeavePayload() implements CustomPacketPayload {

    public static final Type<GameLeavePayload> TYPE = new Type<>(ChartaMod.id("game_leave"));

    public static StreamCodec<ByteBuf, GameLeavePayload> STREAM_CODEC = StreamCodec.unit(new GameLeavePayload());

    public static void handleServer(GameLeavePayload payload, ServerPlayer player, Executor executor) {
        executor.execute(player::stopRiding);
    }

    public static void handleClient(GameLeavePayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new ConfirmScreen(null, Component.translatable("message.charta.leaving_game"), true, () -> {
                if(minecraft.player != null) {
                    minecraft.player.stopRiding();
                    ChartaMod.getPacketManager().sendToServer(new GameLeavePayload());
                }
            }));
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
