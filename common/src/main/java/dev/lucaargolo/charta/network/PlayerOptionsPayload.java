package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.Executor;

public record PlayerOptionsPayload(HashMap<ResourceLocation, byte[]> playerOptions) implements CustomPacketPayload {

    public static final Type<PlayerOptionsPayload> TYPE = new Type<>(ChartaMod.id("player_options"));

    public static StreamCodec<ByteBuf, PlayerOptionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BYTE_ARRAY),
            PlayerOptionsPayload::playerOptions,
            PlayerOptionsPayload::new
    );

    public static void handleClient(PlayerOptionsPayload payload, Executor executor) {
        executor.execute(() -> {
            ChartaClient.LOCAL_OPTIONS.clear();
            ChartaClient.LOCAL_OPTIONS.putAll(payload.playerOptions);
        });
    }

    public static void handleServer(PlayerOptionsPayload payload, ServerPlayer player, Executor executor) {
        executor.execute(() -> {
            PlayerOptionData data = player.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData.factory(), "charta_player_options");
            data.setPlayerOptions(player, payload.playerOptions());
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
