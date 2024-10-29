package dev.lucaargolo.charta.client;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class ChartaClient {

    private static final ResourceLocation MISSING_CARD = Charta.id("missing_card");

    public static final HashMap<ResourceLocation, CardImage> cardImages = new HashMap<>();
    public static final HashMap<ResourceLocation, CardImage> deckImages = new HashMap<>();

    public static void generateImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.register(MISSING_CARD, CardImageUtils.convertCardImage(CardImageUtils.EMPTY));
        cardImages.forEach((id, image) -> {
            ResourceLocation cardId = ChartaClient.getCardTexture(id);
            manager.register(cardId, CardImageUtils.convertCardImage(image));
        });
        deckImages.forEach((id, image) -> {
            ResourceLocation deckId = ChartaClient.getDeckTexture(id);
            manager.register(deckId, CardImageUtils.convertCardImage(image));
        });
    }

    public static void putCardImages(HashMap<ResourceLocation, CardImage> cardImages) {
        ChartaClient.cardImages.putAll(cardImages);
    }

    public static ResourceLocation getCardTexture(ResourceLocation location) {
        if (cardImages.containsKey(location)) {
            return location.withPath(s -> "card/"+s);
        }else{
            return MISSING_CARD;
        }
    }

    public static void putDeckImages(HashMap<ResourceLocation, CardImage> deckImages) {
        ChartaClient.deckImages.putAll(deckImages);
    }

    public static ResourceLocation getDeckTexture(ResourceLocation location) {
        if (deckImages.containsKey(location)) {
            return location.withPath(s -> "deck/"+s);
        }else{
            return MISSING_CARD;
        }
    }

    public static void clearImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.release(MISSING_CARD);
        cardImages.keySet().stream().map(ChartaClient::getCardTexture).forEach(manager::release);
        deckImages.keySet().stream().map(ChartaClient::getDeckTexture).forEach(manager::release);
        cardImages.clear();
        deckImages.clear();
    }

}
