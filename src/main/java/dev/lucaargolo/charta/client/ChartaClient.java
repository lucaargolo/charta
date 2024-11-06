package dev.lucaargolo.charta.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.client.blockentity.CardTableBlockEntityRenderer;
import dev.lucaargolo.charta.client.entity.IronLeashKnotRenderer;
import dev.lucaargolo.charta.client.gui.screens.CrazyEightsScreen;
import dev.lucaargolo.charta.client.item.DeckItemExtensions;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.io.IOException;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ChartaClient {

    public static ShaderInstance CARD_SHADER;
    public static Uniform CARD_FOV;
    public static Uniform CARD_X_ROT;
    public static Uniform CARD_Y_ROT;
    public static Uniform CARD_INSET;
    public static ShaderInstance IRON_LEASH_SHADER;

    public static void generateImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.register(Charta.MISSING_CARD, CardImageUtils.convertCardImage(CardImageUtils.EMPTY));
        Charta.CARD_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation cardId = ChartaClient.getCardTexture(id);
            manager.register(cardId, CardImageUtils.convertCardImage(image));
        });
        Charta.DECK_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation deckId = ChartaClient.getDeckTexture(id);
            manager.register(deckId, CardImageUtils.convertCardImage(image));
        });
    }

    public static ResourceLocation getCardTexture(ResourceLocation location) {
        if (Charta.CARD_IMAGES.getImages().containsKey(location)) {
            return location.withSuffix("card/");
        }else{
            return Charta.MISSING_CARD;
        }
    }

    public static ResourceLocation getDeckTexture(ResourceLocation location) {
        if (Charta.DECK_IMAGES.getImages().containsKey(location)) {
            return location.withSuffix("deck/");
        }else{
            return Charta.MISSING_CARD;
        }
    }

    public static void clearImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.release(Charta.MISSING_CARD);
        Charta.CARD_IMAGES.getImages().keySet().stream().map(ChartaClient::getCardTexture).forEach(manager::release);
        Charta.DECK_IMAGES.getImages().keySet().stream().map(ChartaClient::getDeckTexture).forEach(manager::release);
        Charta.CARD_IMAGES.getImages().clear();
        Charta.DECK_IMAGES.getImages().clear();
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerCoverModel(ModelEvent.RegisterAdditional event) {
            event.register(new ModelResourceLocation(Charta.id("deck"), "standalone"));
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(new DeckItemExtensions(), ModItems.DECK.get());
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntityTypes.SEAT.get(), NoopRenderer::new);
            event.registerEntityRenderer(ModEntityTypes.IRON_LEASH_KNOT.get(), IronLeashKnotRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntityTypes.CARD_TABLE.get(), CardTableBlockEntityRenderer::new);
        }


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
            event.registerShader(new ShaderInstance(event.getResourceProvider(), Charta.id("rendertype_iron_leash"), DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), instance -> {
                IRON_LEASH_SHADER = instance;
            });
        }

    }

    //TODO: Use this method to get the entity hand outside of the CardMenu
    public static List<Card> getEntityHand(LivingEntity entity) {
        return entity.getEntityData().get(Charta.ENTITY_HAND);
    }

}
