package dev.lucaargolo.charta.client.compat;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class IrisCompat {

    public static boolean isPresent() {
        return ChartaMod.getInstance().isModLoaded("iris") || ChartaMod.getInstance().isModLoaded("oculus");
    }

    public static void generateImages() {
        if(IrisCompat.isPresent()) {
            Minecraft client = Minecraft.getInstance();
            TextureManager manager = client.getTextureManager();
            ChartaMod.SUIT_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation cardId = getSuitGlowTexture(id);
                manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
            ChartaMod.CARD_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation cardId = getCardGlowTexture(id);
                manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
            ChartaMod.DECK_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation deckId = getDeckGlowTexture(id);
                manager.register(deckId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
        }
    }

    public static void clearImages() {
        if(IrisCompat.isPresent()) {
            Minecraft client = Minecraft.getInstance();
            TextureManager manager = client.getTextureManager();
            ChartaMod.SUIT_IMAGES.getImages().keySet().stream().map(IrisCompat::getSuitGlowTexture).forEach(manager::release);
            ChartaMod.CARD_IMAGES.getImages().keySet().stream().map(IrisCompat::getCardGlowTexture).forEach(manager::release);
            ChartaMod.DECK_IMAGES.getImages().keySet().stream().map(IrisCompat::getDeckGlowTexture).forEach(manager::release);
        }
    }

    public static ResourceLocation getSuitGlowTexture(ResourceLocation location) {
        if (ChartaMod.SUIT_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("suit_glow/");
        }else{
            return ChartaMod.MISSING_SUIT;
        }
    }

    public static ResourceLocation getCardGlowTexture(ResourceLocation location) {
        if (ChartaMod.CARD_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("card_glow/");
        }else{
            return ChartaMod.MISSING_CARD;
        }
    }

    public static ResourceLocation getDeckGlowTexture(ResourceLocation location) {
        if (ChartaMod.DECK_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("deck_glow/");
        }else{
            return ChartaMod.MISSING_CARD;
        }
    }


}
