package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.addon.ChartaAddonRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public abstract class ModPacketManager {

    public void init() {
        register(PacketInfo.PLAY_TO_CLIENT, ImagesPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, CardDecksPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, UpdateCardContainerSlotPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, UpdateCardContainerCarriedPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, TableScreenPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, GameSlotCompletePayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, GameSlotPositionPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, GameSlotResetPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, GameStartPayload.class);
        register(PacketInfo.PLAY_TO_CLIENT, CardPlayPayload.class);

        register(PacketInfo.PLAY_TO_SERVER, CardContainerSlotClickPayload.class);
        register(PacketInfo.PLAY_TO_SERVER, CardTableSelectGamePayload.class);
        register(PacketInfo.PLAY_TO_SERVER, RestoreSolitairePayload.class);

        register(PacketInfo.PLAY_TO_BOTH, LastFunPayload.class);
        register(PacketInfo.PLAY_TO_BOTH, PlayerOptionsPayload.class);
        register(PacketInfo.PLAY_TO_BOTH, GameLeavePayload.class);

        // Apply any packets registered by addons before init() was called
        ChartaAddonRegistry.applyDeferredPackets(this);
    }

    protected abstract <T extends CustomPacketPayload> void register(PacketInfo info, Class<T> klass);

    /**
     * Public API for addon packet registration.
     * Delegates to the platform-specific {@link #register} method.
     */
    public final <T extends CustomPacketPayload> void registerPacket(PacketDirection direction, Class<T> klass) {
        register(direction.toPacketInfo(), klass);
    }

    /**
     * Register a client-side handler for a payload type.
     * Call from your CLIENT initializer only.
     * Platform implementations override this to wire up the actual handler.
     */
    public <T extends CustomPacketPayload> void registerClientHandler(Class<T> klass, java.util.function.Consumer<T> handler) {
        // Default no-op — platform subclasses override this
    }

    public abstract void sendToServer(CustomPacketPayload payload);

    public abstract void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    public abstract void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload);

    public abstract void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius, CustomPacketPayload payload);

    public abstract void sendToAllPlayers(MinecraftServer server, CustomPacketPayload payload);

    public abstract void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);

    public abstract void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload);

    public abstract void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload);

    protected enum PacketInfo {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER,
        PLAY_TO_BOTH
    }

    /**
     * Public-facing enum for addon packet registration direction.
     * Maps to the internal {@link PacketInfo}.
     */
    public enum PacketDirection {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER,
        PLAY_TO_BOTH;

        PacketInfo toPacketInfo() {
            return PacketInfo.valueOf(this.name());
        }
    }

}
