package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.SuitImage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;

public class ImagesPayload implements CustomPacketPayload {

    private final HashMap<ResourceLocation, SuitImage> suitImages;
    private final HashMap<ResourceLocation, CardImage> cardImages;
    private final HashMap<ResourceLocation, CardImage> deckImages;

    public ImagesPayload(HashMap<ResourceLocation, SuitImage> suitImages, HashMap<ResourceLocation, CardImage> cardImages, HashMap<ResourceLocation, CardImage> deckImages) {
        this.suitImages = suitImages;
        this.cardImages = cardImages;
        this.deckImages = deckImages;
    }

    public ImagesPayload(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.suitImages = new HashMap<>();
        for (int i = 0; i < size; i++) {
            this.suitImages.put(buf.readResourceLocation(), SuitImage.decompress(buf.readByteArray()));
        }
        size = buf.readInt();
        this.cardImages = new HashMap<>();
        for (int i = 0; i < size; i++) {
            this.cardImages.put(buf.readResourceLocation(), CardImage.decompress(buf.readByteArray()));
        }
        size = buf.readInt();
        this.deckImages = new HashMap<>();
        for (int i = 0; i < size; i++) {
            this.deckImages.put(buf.readResourceLocation(), CardImage.decompress(buf.readByteArray()));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(suitImages.size());
        suitImages.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeByteArray(value.compress());
        });
        buf.writeInt(cardImages.size());
        cardImages.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeByteArray(value.compress());
        });
        buf.writeInt(deckImages.size());
        deckImages.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeByteArray(value.compress());
        });
    }

    public static void handleClient(ImagesPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ChartaClient.clearImages();
            Charta.CARD_SUITS.setImages(payload.suitImages);
            Charta.CARD_IMAGES.setImages(payload.cardImages);
            Charta.DECK_IMAGES.setImages(payload.deckImages);
            ChartaClient.generateImages();
        });
    }


}
