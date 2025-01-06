package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record GameStartPayload() implements CustomPacketPayload {

    public static final Type<GameStartPayload> TYPE = new Type<>(Charta.id("game_start"));

    public static StreamCodec<ByteBuf, GameStartPayload> STREAM_CODEC = StreamCodec.unit(new GameStartPayload());

    public static void handleClient(Player player, GameStartPayload payload) {
        onGameStart();
    }

    @Environment(EnvType.CLIENT)
    private static void onGameStart() {
        ChartaClient.LOCAL_HISTORY.clear();
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
