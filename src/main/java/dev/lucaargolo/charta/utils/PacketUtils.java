package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.network.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.PacketDistributor;

public class PacketUtils {

    public static <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload) {
        Charta.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static <T extends CustomPacketPayload> void sendToAllPlayers(T payload) {
        Charta.NETWORK.send(PacketDistributor.ALL.noArg(), payload);
    }

    public static <T extends CustomPacketPayload> void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, T payload) {
        Charta.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z)), payload);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T payload) {
        Charta.NETWORK.sendToServer(payload);
    }

}
