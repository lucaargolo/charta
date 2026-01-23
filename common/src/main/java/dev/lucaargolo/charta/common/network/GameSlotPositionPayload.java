package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.common.game.api.GameSlot;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record GameSlotPositionPayload(BlockPos pos, int index, float x, float y, float z, float angle) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GameSlotPositionPayload> TYPE = new CustomPacketPayload.Type<>(ChartaMod.id("game_slot_position"));

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

    public static void handleClient(GameSlotPositionPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            if(level != null) {
                level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                    GameSlot slot = cardTable.getSlot(payload.index);
                    slot.setX(payload.x);
                    slot.setY(payload.y);
                    slot.setZ(payload.z);
                    slot.setAngle(payload.angle);
                });
            }
        });
    }

}
