package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record CardImagesPayload(HashMap<ResourceLocation, CardImage> cardImages, HashMap<ResourceLocation, CardImage> deckImages) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CardImagesPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("card_images"));

    public static final StreamCodec<ByteBuf, CardImagesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.STREAM_CODEC),
        CardImagesPayload::cardImages,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.STREAM_CODEC),
        CardImagesPayload::deckImages,
        CardImagesPayload::new
    );

    @OnlyIn(Dist.CLIENT)
    public static void handleClient(CardImagesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ChartaClient.clearImages();
            ChartaClient.putCardImages(payload.cardImages);
            ChartaClient.putDeckImages(payload.deckImages);
            ChartaClient.generateImages();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
