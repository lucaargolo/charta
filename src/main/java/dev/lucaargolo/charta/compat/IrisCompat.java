package dev.lucaargolo.charta.compat;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class IrisCompat {

    public static boolean isPresent() {
        return ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus");
    }

    public static void generateImages() {
        if(IrisCompat.isPresent()) {
            Minecraft client = Minecraft.getInstance();
            TextureManager manager = client.getTextureManager();
            Charta.SUIT_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation cardId = getSuitGlowTexture(id);
                manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
            Charta.CARD_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation cardId = getCardGlowTexture(id);
                manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
            Charta.DECK_IMAGES.getImages().forEach((id, image) -> {
                ResourceLocation deckId = getDeckGlowTexture(id);
                manager.register(deckId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), true));
            });
        }
    }

    public static void clearImages() {
        if(IrisCompat.isPresent()) {
            Minecraft client = Minecraft.getInstance();
            TextureManager manager = client.getTextureManager();
            Charta.SUIT_IMAGES.getImages().keySet().stream().map(IrisCompat::getSuitGlowTexture).forEach(manager::release);
            Charta.CARD_IMAGES.getImages().keySet().stream().map(IrisCompat::getCardGlowTexture).forEach(manager::release);
            Charta.DECK_IMAGES.getImages().keySet().stream().map(IrisCompat::getDeckGlowTexture).forEach(manager::release);
        }
    }

    public static ResourceLocation getSuitGlowTexture(ResourceLocation location) {
        if (Charta.SUIT_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("suit_glow/");
        }else{
            return Charta.MISSING_SUIT;
        }
    }

    public static ResourceLocation getCardGlowTexture(ResourceLocation location) {
        if (Charta.CARD_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("card_glow/");
        }else{
            return Charta.MISSING_CARD;
        }
    }

    public static ResourceLocation getDeckGlowTexture(ResourceLocation location) {
        if (Charta.DECK_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("deck_glow/");
        }else{
            return Charta.MISSING_CARD;
        }
    }


}
