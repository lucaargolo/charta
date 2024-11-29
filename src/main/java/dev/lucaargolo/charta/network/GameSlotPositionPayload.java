package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.utils.GameSlot;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record GameSlotPositionPayload(BlockPos pos, int index, float x, float y, float z, float angle) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GameSlotPositionPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("game_slot_position"));

    public static StreamCodec<ByteBuf, GameSlotPositionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            GameSlotPositionPayload::pos,
            ByteBufCodecs.INT,
            GameSlotPositionPayload::index,
            ByteBufCodecs.FLOAT,
            GameSlotPositionPayload::x,
            ByteBufCodecs.FLOAT,
            GameSlotPositionPayload::y,
            ByteBufCodecs.FLOAT,
            GameSlotPositionPayload::z,
            ByteBufCodecs.FLOAT,
            GameSlotPositionPayload::angle,
            GameSlotPositionPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(GameSlotPositionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            updateGameSlot(payload);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateGameSlot(GameSlotPositionPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                GameSlot slot = cardTable.getGameSlot(payload.index);
                slot.setX(payload.x);
                slot.setY(payload.y);
                slot.setZ(payload.z);
                slot.setAngle(payload.angle);
            });
        }
    }

}
