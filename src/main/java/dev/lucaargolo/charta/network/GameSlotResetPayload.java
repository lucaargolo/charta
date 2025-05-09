package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record GameSlotResetPayload(BlockPos pos) implements CustomPacketPayload  {

    public static final Type<GameSlotResetPayload> TYPE = new Type<>(Charta.id("game_slot_reset"));

    public static StreamCodec<ByteBuf, GameSlotResetPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        GameSlotResetPayload::pos,
        GameSlotResetPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(GameSlotResetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> resetGameSlots(payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void resetGameSlots(GameSlotResetPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(CardTableBlockEntity::resetSlots);
        }
    }

}
