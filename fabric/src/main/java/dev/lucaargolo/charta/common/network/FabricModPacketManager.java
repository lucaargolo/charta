package dev.lucaargolo.charta.common.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class FabricModPacketManager extends ModPacketManager {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T extends CustomPacketPayload> void register(PacketInfo info, Class<T> klass) {
        try {
            CustomPacketPayload.Type payloadType = (CustomPacketPayload.Type) klass.getField("TYPE").get(null);
            StreamCodec payloadCodec = (StreamCodec) klass.getField("STREAM_CODEC").get(null);
            if(info == PacketInfo.PLAY_TO_CLIENT || info == PacketInfo.PLAY_TO_BOTH) {
                PayloadTypeRegistry.playS2C().register(payloadType, payloadCodec);
                if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    ((Runnable) () -> {
                        try {
                            Method method = klass.getMethod("handleClient", klass, Executor.class);
                            ClientPlayNetworking.registerGlobalReceiver(payloadType, (data, context) -> {
                                try {
                                    method.invoke(null, data, context.client());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).run();
                }
            }
            if(info == PacketInfo.PLAY_TO_SERVER || info == PacketInfo.PLAY_TO_BOTH) {
                PayloadTypeRegistry.playC2S().register(payloadType, payloadCodec);
                Method method = klass.getMethod("handleServer", klass, ServerPlayer.class, Executor.class);
                ServerPlayNetworking.registerGlobalReceiver(payloadType, (data, context) -> {
                    try {
                        method.invoke(null, data, context.player(), context.server());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
        level.getServer().getPlayerList().broadcastAll(ServerPlayNetworking.createS2CPacket(payload), level.dimension());
    }

    @Override
    public void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius, CustomPacketPayload payload) {
        level.getServer().getPlayerList().broadcast(excluded, x, y, z, radius, level.dimension(), ServerPlayNetworking.createS2CPacket(payload));
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, CustomPacketPayload payload) {
        server.getPlayerList().broadcastAll(ServerPlayNetworking.createS2CPacket(payload));
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("Cannot send clientbound payloads on the client");
        }else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.broadcast(entity, ServerPlayNetworking.createS2CPacket(payload));
        }
    }

    @Override
    public void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("Cannot send clientbound payloads on the client");
        } else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.broadcastAndSend(entity, ServerPlayNetworking.createS2CPacket(payload));
        }
    }

    @Override
    public void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
        Packet<?> packet = ServerPlayNetworking.createS2CPacket(payload);
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            player.connection.send(packet);
        }
    }
}
