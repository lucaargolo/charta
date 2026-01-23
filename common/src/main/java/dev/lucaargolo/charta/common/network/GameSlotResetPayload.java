package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.entity.CardTableBlockEntity;
import dev.lucaargolo.charta.common.block.entity.ModBlockEntityTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record GameSlotResetPayload(BlockPos pos) implements CustomPacketPayload  {

    public static final Type<GameSlotResetPayload> TYPE = new Type<>(ChartaMod.id("game_slot_reset"));

    public static StreamCodec<ByteBuf, GameSlotResetPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        GameSlotResetPayload::pos,
        GameSlotResetPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(GameSlotResetPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            if(level != null) {
                level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(CardTableBlockEntity::resetSlots);
            }
        });
    }

}
