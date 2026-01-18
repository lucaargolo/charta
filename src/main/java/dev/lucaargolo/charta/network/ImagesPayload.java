package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import dev.lucaargolo.charta.utils.SuitImage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record ImagesPayload(HashMap<ResourceLocation, SuitImage> suitImages, HashMap<ResourceLocation, CardImage> cardImages, HashMap<ResourceLocation, CardImage> deckImages) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ImagesPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("card_images"));

    public static final StreamCodec<ByteBuf, ImagesPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.SUIT_STREAM_CODEC),
        ImagesPayload::suitImages,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
        ImagesPayload::cardImages,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardImageUtils.CARD_STREAM_CODEC),
        ImagesPayload::deckImages,
        ImagesPayload::new
    );

    public static void handleClient(Player player, ImagesPayload payload) {
        ChartaClient.clearImages();
        Charta.SUIT_IMAGES.setImages(payload.suitImages());
        Charta.CARD_IMAGES.setImages(payload.cardImages());
        Charta.DECK_IMAGES.setImages(payload.deckImages());
        ChartaClient.generateImages();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
