package dev.lucaargolo.charta.client;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.client.compat.IrisCompat;
import dev.lucaargolo.charta.client.render.ModRenderTypeManager;
import dev.lucaargolo.charta.client.render.ModShaderManager;
import dev.lucaargolo.charta.client.render.block.BarShelfBlockEntityRenderer;
import dev.lucaargolo.charta.client.render.block.CardTableBlockEntityRenderer;
import dev.lucaargolo.charta.client.render.entity.IronLeashKnotRenderer;
import dev.lucaargolo.charta.client.render.item.DeckItemRenderer;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsScreen;
import dev.lucaargolo.charta.game.fun.FunScreen;
import dev.lucaargolo.charta.game.solitaire.SolitaireScreen;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenuTypes;
import dev.lucaargolo.charta.registry.ModItemRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.resources.MarkdownResource;
import dev.lucaargolo.charta.utils.CardImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class ChartaModClient {

    private static ChartaModClient instance;

    public static final LinkedList<Triple<Component, Integer, Component>> LOCAL_HISTORY = new LinkedList<>();
    public static final HashMap<ResourceLocation, byte[]> LOCAL_OPTIONS = new HashMap<>();

    public static final MarkdownResource MARKDOWN = new MarkdownResource();

    private final ModShaderManager shaderManager;
    private final ModRenderTypeManager renderTypeManager;

    public ChartaModClient() {
        instance = this;
        this.shaderManager = ChartaMod.loadPlatformClass(ModShaderManager.class);
        this.renderTypeManager = ChartaMod.loadPlatformClass(ModRenderTypeManager.class);
    }

    protected final void init() {
        this.shaderManager.init();

        this.registerEntityRenderer(ModEntityTypes.SEAT, NoopRenderer::new);
        this.registerEntityRenderer(ModEntityTypes.IRON_LEASH_KNOT, IronLeashKnotRenderer::new);

        this.registerBlockEntityRenderer(ModBlockEntityTypes.CARD_TABLE, CardTableBlockEntityRenderer::new);
        this.registerBlockEntityRenderer(ModBlockEntityTypes.BAR_SHELF, BarShelfBlockEntityRenderer::new);

        this.registerMenuScreen(ModMenuTypes.CRAZY_EIGHTS, CrazyEightsScreen::new);
        this.registerMenuScreen(ModMenuTypes.FUN, FunScreen::new);
        this.registerMenuScreen(ModMenuTypes.SOLITAIRE, SolitaireScreen::new);

        this.registerAdditionalModel(ChartaMod.id("deck"));
        this.registerDynamicItemRenderer(ModItems.DECK, new DeckItemRenderer());
        this.registerReloadableListener(ChartaMod.id("markdown"), MARKDOWN);
    }

    public abstract BakedModel getModel(ResourceLocation location);

    protected abstract <M extends AbstractContainerMenu, P extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, P> factory);

    protected abstract <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider);

    protected abstract <E extends BlockEntity, P extends BlockEntityRendererProvider<E>> void registerBlockEntityRenderer(MinecraftEntry<BlockEntityType<E>> type, P provider);

    protected abstract void registerAdditionalModel(ResourceLocation location);

    protected abstract void registerDynamicItemRenderer(ModItemRegistry.ItemEntry<?> item, BlockEntityWithoutLevelRenderer itemRenderer);

    protected abstract void registerReloadableListener(ResourceLocation identifier, PreparableReloadListener listener);

    public static ChartaModClient getInstance() {
        return instance;
    }

    public static ModShaderManager getShaderManager() {
        return instance.shaderManager;
    }

    public static ModRenderTypeManager getRenderTypeManager() {
        return instance.renderTypeManager;
    }

    public static void generateImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.register(ChartaMod.MISSING_SUIT, CardImageUtils.convertImage(CardImageUtils.EMPTY_SUIT, IrisCompat.isPresent(), false));
        ChartaMod.SUIT_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation suitId = ChartaModClient.getSuitTexture(id);
            manager.register(suitId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        manager.register(ChartaMod.MISSING_CARD, CardImageUtils.convertImage(CardImageUtils.EMPTY_CARD, IrisCompat.isPresent(), false));
        ChartaMod.CARD_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation cardId = ChartaModClient.getCardTexture(id);
            manager.register(cardId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        ChartaMod.DECK_IMAGES.getImages().forEach((id, image) -> {
            ResourceLocation deckId = ChartaModClient.getDeckTexture(id);
            manager.register(deckId, CardImageUtils.convertImage(image, IrisCompat.isPresent(), false));
        });
        IrisCompat.generateImages();
    }

    public static ResourceLocation getSuitTexture(ResourceLocation location) {
        if (ChartaMod.SUIT_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("suit/");
        }else{
            return ChartaMod.MISSING_SUIT;
        }
    }

    public static ResourceLocation getCardTexture(ResourceLocation location) {
        if (ChartaMod.CARD_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("card/");
        }else{
            return ChartaMod.MISSING_CARD;
        }
    }

    public static ResourceLocation getDeckTexture(ResourceLocation location) {
        if (ChartaMod.DECK_IMAGES.getImages().containsKey(location)) {
            return location.withPrefix("deck/");
        }else{
            return ChartaMod.MISSING_CARD;
        }
    }

    public static void clearImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager manager = client.getTextureManager();
        manager.release(ChartaMod.MISSING_SUIT);
        ChartaMod.SUIT_IMAGES.getImages().keySet().stream().map(ChartaModClient::getSuitTexture).forEach(manager::release);
        ChartaMod.SUIT_IMAGES.getImages().clear();
        manager.release(ChartaMod.MISSING_CARD);
        ChartaMod.CARD_IMAGES.getImages().keySet().stream().map(ChartaModClient::getCardTexture).forEach(manager::release);
        ChartaMod.DECK_IMAGES.getImages().keySet().stream().map(ChartaModClient::getDeckTexture).forEach(manager::release);
        IrisCompat.clearImages();
        ChartaMod.CARD_IMAGES.getImages().clear();
        ChartaMod.DECK_IMAGES.getImages().clear();
    }

}
