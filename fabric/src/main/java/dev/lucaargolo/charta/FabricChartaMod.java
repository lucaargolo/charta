package dev.lucaargolo.charta;

import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.registry.ModMenuTypeRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FabricChartaMod extends ChartaMod implements ModInitializer {

    public static final List<BiConsumer<LevelChunk, ServerPlayer>> ON_CHUNK_SENT = new ArrayList<>();

    @Override
    public void onInitialize() {
        this.init();
        ModPoiTypes.REGISTRY.getEntries().forEach(entry -> {
            PointOfInterestHelper.register(entry.key(), entry.get().maxTickets(), entry.get().validRange(), ModBlocks.DEALER_TABLE.get());
        });
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    protected void registerReloadableListener(ResourceLocation identifier, PreparableReloadListener listener) {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
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

    @Override
    protected void addVillagerTrade(MinecraftEntry<VillagerProfession> profession, int level, VillagerTrades.ItemListing listing) {
        TradeOfferHelper.registerVillagerOffers(profession.get(), level, factory -> {
            factory.add(listing);
        });
    }

    @Override
    public <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, BiFunction<Integer, Inventory, M> constructor, Player player, D data, Component title) {
        player.openMenu(new ExtendedScreenHandlerFactory<D>() {
            @Override
            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return constructor.apply(i, inventory);
            }

            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public D getScreenOpeningData(ServerPlayer serverPlayer) {
                return data;
            }
        });
    }

    @Override
    public void registerEventOnServerAboutToStart(Consumer<MinecraftServer> consumer) {
        ServerLifecycleEvents.SERVER_STARTING.register(consumer::accept);
    }

    @Override
    public void registerEventOnChunkSent(BiConsumer<LevelChunk, ServerPlayer> consumer) {
        ON_CHUNK_SENT.add(consumer);
    }

    @Override
    public void registerEventOnPlayerJoined(Consumer<ServerPlayer> consumer) {
        ServerPlayerEvents.JOIN.register(consumer::accept);
    }

    @Override
    public void registerEventOnDatapackReload(Consumer<MinecraftServer> consumer) {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> consumer.accept(server));
    }

}
