package dev.lucaargolo.charta.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.client.blockentity.BarShelfBlockEntityRenderer;
import dev.lucaargolo.charta.client.blockentity.CardTableBlockEntityRenderer;
import dev.lucaargolo.charta.client.entity.IronLeashKnotRenderer;
import dev.lucaargolo.charta.client.item.DeckItemExtensions;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsScreen;
import dev.lucaargolo.charta.game.fun.FunScreen;
import dev.lucaargolo.charta.game.solitaire.SolitaireScreen;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.resources.MarkdownResource;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ChartaClient implements ClientModInitializer {

    public static final LinkedList<Triple<Component, Integer, Component>> LOCAL_HISTORY = new LinkedList<>();
    public static final HashMap<ResourceLocation, byte[]> LOCAL_OPTIONS = new HashMap<>();
    private static final ResourceLocation BLUR_LOCATION = Charta.id("shaders/post/blur.json");

    private static RenderTarget glowRenderTarget;
    private static PostChain glowBlurEffect;

    public static ShaderInstance IMAGE_SHADER;
    public static ShaderInstance IMAGE_GLOW_SHADER;
    public static ShaderInstance IMAGE_ARGB_SHADER;
    public static ShaderInstance WHITE_IMAGE_SHADER;
    public static ShaderInstance WHITE_IMAGE_GLOW_SHADER;
    public static ShaderInstance WHITE_IMAGE_ARGB_SHADER;
    public static ShaderInstance CARD_SHADER;
    public static ShaderInstance CARD_GLOW_SHADER;
    public static ShaderInstance CARD_ARGB_SHADER;
    public static ShaderInstance PERSPECTIVE_SHADER;
    public static ShaderInstance GRAYSCALE_SHADER;

    private static final List<Consumer<Float>> cardFovUniforms = new ArrayList<>();
    public static Consumer<Float> CARD_FOV = f -> cardFovUniforms.forEach(c -> c.accept(f));
    private static final List<Consumer<Float>> cardXRotUniforms = new ArrayList<>();
    public static Consumer<Float> CARD_X_ROT = f -> cardXRotUniforms.forEach(c -> c.accept(f));
    private static final List<Consumer<Float>> cardYRotUniforms = new ArrayList<>();
    public static Consumer<Float> CARD_Y_ROT = f -> cardYRotUniforms.forEach(c -> c.accept(f));
    private static final List<Consumer<Float>> cardInsetUniforms = new ArrayList<>();
    public static Consumer<Float> CARD_INSET = f -> cardInsetUniforms.forEach(c -> c.accept(f));

    public static ShaderInstance ENTITY_CARD_SHADER;
    public static ShaderInstance IRON_LEASH_SHADER;

    public static final MarkdownResource MARKDOWN = new MarkdownResource();

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), ModBlocks.BERRY_WINE_GLASS, ModBlocks.CACTUS_WINE_GLASS, ModBlocks.EMPTY_WINE_GLASS);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), ModBlocks.SORGHUM_BEER_GLASS, ModBlocks.WHEAT_BEER_GLASS, ModBlocks.EMPTY_BEER_GLASS);
        ClientModEvents.onClientSetup(Minecraft.getInstance());
        ModelLoadingPlugin.register(ClientModEvents::registerCoverModel);
        ClientModEvents.registerClientExtensions();
        ClientModEvents.registerEntityRenderers();
        ClientModEvents.registerMenuScreens();
        ClientModEvents.addReloadListeners(ResourceManagerHelper.get(PackType.CLIENT_RESOURCES));
        CoreShaderRegistrationCallback.EVENT.register(ClientModEvents::registerShaders);
    }

    public static void generateImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.register(Charta.MISSING_SUIT, CardImageUtils.convertImage(CardImageUtils.EMPTY_SUIT, IrisCompat.isPresent(), false));
        Charta.CARD_SUITS.getImages().forEach((id, image) -> {
            ResourceLocation suitId = ChartaClient.getSuitTexture(id);
            manager.register(suitId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        manager.register(Charta.MISSING_CARD, CardImageUtils.convertImage(CardImageUtils.EMPTY_CARD, IrisCompat.isPresent(), false));
        Charta.CARD_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation cardId = ChartaClient.getCardTexture(id);
            manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        Charta.DECK_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation deckId = ChartaClient.getDeckTexture(id);
            manager.register(deckId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        IrisCompat.generateImages();
    }

    public static ResourceLocation getSuitTexture(ResourceLocation location) {
        if (Charta.CARD_SUITS.getImages().containsKey(location)) {
            return location.withPrefix("suit/");
        }else{
            return Charta.MISSING_SUIT;
        }
    }

    public static ResourceLocation getCardTexture(ResourceLocation location) {
        if (Charta.CARD_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("card/");
        }else{
            return Charta.MISSING_CARD;
        }
    }

    public static ResourceLocation getDeckTexture(ResourceLocation location) {
        if (Charta.DECK_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("deck/");
        }else{
            return Charta.MISSING_CARD;
        }
    }

    public static void clearImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.release(Charta.MISSING_SUIT);
        Charta.CARD_SUITS.getImages().keySet().stream().map(ChartaClient::getSuitTexture).forEach(manager::release);
        Charta.CARD_SUITS.getImages().clear();
        manager.release(Charta.MISSING_CARD);
        Charta.CARD_IMAGES.getImages().keySet().stream().map(ChartaClient::getCardTexture).forEach(manager::release);
        Charta.DECK_IMAGES.getImages().keySet().stream().map(ChartaClient::getDeckTexture).forEach(manager::release);
        IrisCompat.clearImages();
        Charta.CARD_IMAGES.getImages().clear();
        Charta.DECK_IMAGES.getImages().clear();
    }

    public static void processBlurEffect(float partialTick) {
        float f = 2f;
        if (glowBlurEffect != null) {
            glowBlurEffect.setUniform("Radius", f);
            glowBlurEffect.process(partialTick);
        }
    }

    public static RenderTarget getGlowRenderTarget() {
        return glowRenderTarget;
    }

    public static PostChain getGlowBlurEffect() {
        return glowBlurEffect;
    }

    private static void loadGlowBlurEffect(ResourceProvider resourceProvider) {
        Minecraft minecraft = Minecraft.getInstance();

        if (glowBlurEffect != null) {
            glowBlurEffect.close();
        }

        try {
            glowBlurEffect = new PostChain(minecraft.getTextureManager(), resourceProvider, getGlowRenderTarget(), BLUR_LOCATION);
            glowBlurEffect.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        } catch (IOException ioexception) {
            Charta.LOGGER.warn("Failed to load shader: {}", BLUR_LOCATION, ioexception);
        } catch (JsonSyntaxException jsonsyntaxexception) {
            Charta.LOGGER.warn("Failed to parse shader: {}", BLUR_LOCATION, jsonsyntaxexception);
        }
    }

    public static class ClientModEvents {

        public static void onClientSetup(Minecraft minecraft) {
            minecraft.submit(() -> {
                glowRenderTarget = new TextureTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), false, Minecraft.ON_OSX);
                glowRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                glowRenderTarget.clear(Minecraft.ON_OSX);
            });
        }

        public static void registerCoverModel(ModelLoadingPlugin.Context context) {
            context.addModels(Charta.id("deck"));
        }

        public static void registerClientExtensions() {
            BuiltinItemRendererRegistry.INSTANCE.register(ModItems.DECK, new DeckItemExtensions());
        }

        public static void registerEntityRenderers() {
            EntityRendererRegistry.register(ModEntityTypes.SEAT, NoopRenderer::new);
            EntityRendererRegistry.register(ModEntityTypes.IRON_LEASH_KNOT, IronLeashKnotRenderer::new);
            BlockEntityRenderers.register(ModBlockEntityTypes.CARD_TABLE, CardTableBlockEntityRenderer::new);
            BlockEntityRenderers.register(ModBlockEntityTypes.BAR_SHELF, BarShelfBlockEntityRenderer::new);
        }

        public static void addReloadListeners(ResourceManagerHelper event) {
            event.registerReloadListener(MARKDOWN);
        }

        public static void registerMenuScreens() {
            MenuScreens.register(ModMenus.CRAZY_EIGHTS, CrazyEightsScreen::new);
            MenuScreens.register(ModMenus.FUN, FunScreen::new);
            MenuScreens.register(ModMenus.SOLITAIRE, SolitaireScreen::new);
        }

        public static void registerShaders(CoreShaderRegistrationCallback.RegistrationContext context) throws IOException {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            loadGlowBlurEffect(manager);
            cardFovUniforms.clear();
            cardXRotUniforms.clear();
            cardYRotUniforms.clear();
            cardInsetUniforms.clear();
            context.register(Charta.id("image"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                IMAGE_SHADER = instance;
            });
            context.register(Charta.id("image_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                IMAGE_GLOW_SHADER = instance;
            });
            context.register(Charta.id("image_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                IMAGE_ARGB_SHADER = instance;
            });
            context.register(Charta.id("white_image"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                WHITE_IMAGE_SHADER = instance;
            });
            context.register(Charta.id("white_image_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                WHITE_IMAGE_GLOW_SHADER = instance;
            });
            context.register(Charta.id("white_image_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                WHITE_IMAGE_ARGB_SHADER = instance;
            });
            context.register(Charta.id("card"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
                cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
                cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
                cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
                CARD_SHADER = instance;
            });
            context.register(Charta.id("card_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
                cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
                cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
                cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
                CARD_GLOW_SHADER = instance;
            });
            context.register(Charta.id("card_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
                cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
                cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
                cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
                CARD_ARGB_SHADER = instance;
            });
            context.register(Charta.id("perspective"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
                cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
                cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
                cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
                PERSPECTIVE_SHADER = instance;
            });
            context.register(Charta.id("grayscale"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
                GRAYSCALE_SHADER = instance;
            });
            context.register(Charta.id("rendertype_entity_card"), DefaultVertexFormat.NEW_ENTITY, instance -> {
                ENTITY_CARD_SHADER = instance;
            });
            context.register(Charta.id("rendertype_iron_leash"), DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, instance -> {
                IRON_LEASH_SHADER = instance;
            });
        }

    }

    @Nullable
    public static ShaderInstance getImageShader() {
        return IMAGE_SHADER;
    }

    @Nullable
    public static ShaderInstance getImageGlowShader() {
        return IMAGE_GLOW_SHADER;
    }

    @Nullable
    public static ShaderInstance getImageArgbShader() {
        return IMAGE_ARGB_SHADER;
    }

    @Nullable
    public static ShaderInstance getWhiteImageShader() {
        return WHITE_IMAGE_SHADER;
    }

    @Nullable
    public static ShaderInstance getWhiteImageGlowShader() {
        return WHITE_IMAGE_GLOW_SHADER;
    }

    @Nullable
    public static ShaderInstance getWhiteImageArgbShader() {
        return WHITE_IMAGE_ARGB_SHADER;
    }

    @Nullable
    public static ShaderInstance getCardShader() {
        return CARD_SHADER;
    }

    @Nullable
    public static ShaderInstance getCardGlowShader() {
        return CARD_GLOW_SHADER;
    }

    @Nullable
    public static ShaderInstance getCardArgbShader() {
        return CARD_ARGB_SHADER;
    }

    @Nullable
    public static ShaderInstance getPerspectiveShader() {
        return PERSPECTIVE_SHADER;
    }

    @Nullable
    public static ShaderInstance getGrayscaleShader() {
        return GRAYSCALE_SHADER;
    }
}
