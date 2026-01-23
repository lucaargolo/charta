package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.entity.ModBlockEntityTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record CardTableSelectGamePayload(BlockPos pos, ResourceLocation gameId, byte[] options) implements CustomPacketPayload {

    public static final Type<CardTableSelectGamePayload> TYPE = new Type<>(ChartaMod.id("card_table_select_game"));

    public static final StreamCodec<ByteBuf, CardTableSelectGamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CardTableSelectGamePayload::pos,
            ResourceLocation.STREAM_CODEC,
            CardTableSelectGamePayload::gameId,
            ByteBufCodecs.BYTE_ARRAY,
            CardTableSelectGamePayload::options,
            CardTableSelectGamePayload::new
    );

    public static void handleServer(CardTableSelectGamePayload payload, ServerPlayer player, Executor executor) {
        executor.execute(() -> player.level().getBlockEntity(payload.pos(), ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(table -> {
            if(table.getGame() == null || table.getGame().isGameOver()) {
                Component result = table.startGame(payload.gameId(), payload.options());
                player.displayClientMessage(result, true);
            }
        }));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
