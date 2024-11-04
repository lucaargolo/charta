package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.client.gui.screens.CardTableScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenCardTableScreenPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenCardTableScreenPayload> TYPE = new Type<>(Charta.id("open_card_table_screen"));

    public static final StreamCodec<ByteBuf, OpenCardTableScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenCardTableScreenPayload::pos,
            OpenCardTableScreenPayload::new
    );

    public static void handleClient(OpenCardTableScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            openScreen(payload.pos);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new CardTableScreen(pos));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
