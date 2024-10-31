package dev.lucaargolo.charta.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.CrazyEightsScreen;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ChartaClient {

    public static ShaderInstance CARD_SHADER;
    public static Uniform CARD_FOV;
    public static Uniform CARD_X_ROT;
    public static Uniform CARD_Y_ROT;
    public static Uniform CARD_INSET;

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

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.CRAZY_EIGHTS.get(), CrazyEightsScreen::new);
        }

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Charta.id("rendertype_card"), DefaultVertexFormat.BLOCK), instance -> {
                CARD_FOV = instance.getUniform("Fov");
                CARD_X_ROT  = instance.getUniform("XRot");
                CARD_Y_ROT  = instance.getUniform("YRot");
                CARD_INSET  = instance.getUniform("InSet");
                CARD_SHADER = instance;
            });
        }

    }

    public static List<Card> getPlayerHand(Player player) {
        return player.getEntityData().get(Charta.PLAYER_HAND);
    }

    public static List<Card> getVillagerHand(Player player) {
        return player.getEntityData().get(Charta.VILLAGER_HAND);
    }

}
