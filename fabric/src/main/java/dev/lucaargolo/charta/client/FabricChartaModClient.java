package dev.lucaargolo.charta.client;

import dev.lucaargolo.charta.common.block.ModBlocks;
import dev.lucaargolo.charta.common.registry.ModItemRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class FabricChartaModClient extends ChartaModClient implements ClientModInitializer {

    private final List<ResourceLocation> additionalModels = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        this.init();
        ModelLoadingPlugin.register(context -> {
            context.addModels(this.additionalModels);
        });
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EMPTY_BEER_GLASS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WHEAT_BEER_GLASS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SORGHUM_BEER_GLASS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EMPTY_WINE_GLASS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BERRY_WINE_GLASS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CACTUS_WINE_GLASS.get(), RenderType.translucent());
    }

    @Override
    public BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }

    @Override
    protected <M extends AbstractContainerMenu, P extends Screen & MenuAccess<M>> void registerMenuScreen(MinecraftEntry<MenuType<M>> type, TriFunction<M, Inventory, Component, P> factory) {
        MenuScreens.register(type.get(), factory::apply);
    }

    @Override
    protected <E extends Entity, P extends EntityRendererProvider<E>> void registerEntityRenderer(MinecraftEntry<EntityType<E>> type, P provider) {
        EntityRendererRegistry.register(type.get(), provider);
    }

    @Override
    protected <E extends BlockEntity, P extends BlockEntityRendererProvider<E>> void registerBlockEntityRenderer(MinecraftEntry<BlockEntityType<E>> type, P provider) {
        BlockEntityRenderers.register(type.get(), provider);
    }

    @Override
    protected void registerAdditionalModel(ResourceLocation location) {
        this.additionalModels.add(location);
    }

    @Override
    protected void registerDynamicItemRenderer(ModItemRegistry.ItemEntry<?> entry, Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        BuiltinItemRendererRegistry.INSTANCE.register(entry.get(), renderer.get()::renderByItem);
    }

    @Override
    protected void registerReloadableListener(ResourceLocation identifier, PreparableReloadListener listener) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return identifier;
            }

            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return listener.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
    }
}
