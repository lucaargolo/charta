package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CardTableSelectGamePayload(BlockPos pos, ResourceLocation gameId) implements CustomPacketPayload {

    public static final Type<CardTableSelectGamePayload> TYPE = new Type<>(Charta.id("card_table_select_game"));

    public static final StreamCodec<ByteBuf, CardTableSelectGamePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CardTableSelectGamePayload::pos,
            ResourceLocation.STREAM_CODEC,
            CardTableSelectGamePayload::gameId,
            CardTableSelectGamePayload::new
    );

    public static void handleServer(CardTableSelectGamePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().level().getBlockEntity(payload.pos(), ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(table -> {
                if(table.getGame() == null || table.getGame().isGameOver()) {
                    Component result = table.startGame(payload.gameId());
                    context.player().displayClientMessage(result, true);
                }
            });
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
