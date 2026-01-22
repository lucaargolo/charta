package dev.lucaargolo.charta.client;

import dev.lucaargolo.charta.NeoForgeChartaMod;
import dev.lucaargolo.charta.registry.ModItemRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NeoForgeChartaModClient extends ChartaModClient {

    private final Map<Supplier<MenuType>, MenuScreens.ScreenConstructor> menuScreens = new HashMap<>();
    private final Map<Supplier<EntityType>, EntityRendererProvider> entityRenderers = new HashMap<>();
    private final Map<Supplier<BlockEntityType>, BlockEntityRendererProvider> blockEntityRenderers = new HashMap<>();

    private final List<ResourceLocation> additionalModels = new ArrayList<>();
    private final Map<ModItemRegistry.ItemEntry<?>, Supplier<BlockEntityWithoutLevelRenderer>> dynamicItemRenderers = new HashMap<>();
    private final List<PreparableReloadListener> reloadListeners = new ArrayList<>();

    public NeoForgeChartaModClient() {
        this.init();
        NeoForgeChartaMod.getModBus().addListener(RegisterClientReloadListenersEvent.class, event -> this.reloadListeners.forEach(event::registerReloadListener));
        NeoForgeChartaMod.getModBus().addListener(RegisterMenuScreensEvent.class, event -> this.menuScreens.forEach((supplier, constructor) -> event.register(supplier.get(), constructor)));
        NeoForgeChartaMod.getModBus().addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            this.entityRenderers.forEach((supplier, provider) -> event.registerEntityRenderer(supplier.get(), provider));
            this.blockEntityRenderers.forEach((supplier, provider) -> event.registerBlockEntityRenderer(supplier.get(), provider));
        });
        NeoForgeChartaMod.getModBus().addListener(ModelEvent.RegisterAdditional.class, event -> this.additionalModels.forEach(location -> event.register(ModelResourceLocation.standalone(location))));
        NeoForgeChartaMod.getModBus().addListener(RegisterClientExtensionsEvent.class, event -> this.dynamicItemRenderers.forEach((item, renderer) -> event.registerItem(itemClientExtension(renderer), item.get())));
    }

    @Override
    public BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(location));
    }

    @Override
    protected <M extends AbstractContainerMenu, P extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, P> factory) {
        this.menuScreens.put(type::get, (menu, inventory, title) -> factory.apply((M) menu, inventory, title));
    }

    @Override
    protected <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider) {
        this.entityRenderers.put(type::get, provider);
    }

    @Override
    protected <E extends BlockEntity, P extends BlockEntityRendererProvider<E>> void registerBlockEntityRenderer(MinecraftEntry<BlockEntityType<E>> type, P provider) {
        this.blockEntityRenderers.put(type::get, provider);
    }

    @Override
    protected void registerAdditionalModel(ResourceLocation location) {
        this.additionalModels.add(location);
    }

    @Override
    protected void registerDynamicItemRenderer(ModItemRegistry.ItemEntry<?> entry, Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        this.dynamicItemRenderers.put(entry, renderer);
    }

    @Override
    protected void registerReloadableListener(ResourceLocation identifier, PreparableReloadListener listener) {
        this.reloadListeners.add(listener);
    }

    private static IClientItemExtensions itemClientExtension(Supplier<BlockEntityWithoutLevelRenderer> supplier) {
        return new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = supplier.get();

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };
    }


}
