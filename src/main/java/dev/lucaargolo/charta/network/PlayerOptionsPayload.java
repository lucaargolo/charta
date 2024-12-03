package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record PlayerOptionsPayload(HashMap<ResourceLocation, byte[]> playerOptions) implements CustomPacketPayload {

    public static final Type<PlayerOptionsPayload> TYPE = new Type<>(Charta.id("player_options"));

    public static StreamCodec<ByteBuf, PlayerOptionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.BYTE_ARRAY),
            PlayerOptionsPayload::playerOptions,
            PlayerOptionsPayload::new
    );

    public static void handleBoth(PlayerOptionsPayload payload, IPayloadContext context) {
        if(context.flow() == PacketFlow.SERVERBOUND) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleClient(PlayerOptionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ChartaClient.LOCAL_OPTIONS.clear();
            ChartaClient.LOCAL_OPTIONS.putAll(payload.playerOptions);
        });
    }

    public static void handleServer(PlayerOptionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer) {
                PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData.factory(), "charta_player_options");
                data.setPlayerOptions(serverPlayer, payload.playerOptions());
            }
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
