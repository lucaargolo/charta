package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record PlayerOptionsPayload(HashMap<ResourceLocation, byte[]> playerOptions) implements CustomPacketPayload {

    public static final Type<PlayerOptionsPayload> TYPE = new Type<>(Charta.id("player_options"));

    public static StreamCodec<ByteBuf, PlayerOptionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BYTE_ARRAY),
            PlayerOptionsPayload::playerOptions,
            PlayerOptionsPayload::new
    );

    public static void handleClient(Player player, PlayerOptionsPayload payload) {
        ChartaClient.LOCAL_OPTIONS.clear();
        ChartaClient.LOCAL_OPTIONS.putAll(payload.playerOptions);
    }

    public static void handleServer(Player player, PlayerOptionsPayload payload) {
        if(player instanceof ServerPlayer serverPlayer) {
            PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData.factory(), "charta_player_options");
            data.setPlayerOptions(serverPlayer, payload.playerOptions());
        }
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
