package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.utils.CardImage;
import dev.lucaargolo.charta.common.utils.CardImageUtils;
import dev.lucaargolo.charta.common.utils.SuitImage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.Executor;

public record ImagesPayload(HashMap<ResourceLocation, SuitImage> suitImages, HashMap<ResourceLocation, CardImage> cardImages, HashMap<ResourceLocation, CardImage> deckImages) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ImagesPayload> TYPE = new CustomPacketPayload.Type<>(ChartaMod.id("card_images"));

    public static final StreamCodec<ByteBuf, ImagesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.SUIT_STREAM_CODEC),
        ImagesPayload::suitImages,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
        ImagesPayload::cardImages,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
        ImagesPayload::deckImages,
        ImagesPayload::new
    );

    public static void handleClient(ImagesPayload payload, Executor executor) {
        executor.execute(() -> {
            ChartaModClient.clearImages();
            ChartaMod.SUIT_IMAGES.setImages(payload.suitImages());
            ChartaMod.CARD_IMAGES.setImages(payload.cardImages());
            ChartaMod.DECK_IMAGES.setImages(payload.deckImages());
            ChartaModClient.generateImages();
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
