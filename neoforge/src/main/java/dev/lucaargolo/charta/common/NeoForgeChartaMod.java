package dev.lucaargolo.charta.common;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.common.registry.ModMenuTypeRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(dev.lucaargolo.charta.common.ChartaMod.MOD_ID)
public class NeoForgeChartaMod extends dev.lucaargolo.charta.common.ChartaMod {

    private final List<PreparableReloadListener> reloadListeners = new ArrayList<>();
    private final Map<MinecraftEntry<VillagerProfession>, List<Pair<Integer, Supplier<VillagerTrades.ItemListing>>>> villagerTrades = new HashMap<>();
    private final List<Consumer<MinecraftServer>> onServerAboutToStart = new ArrayList<>();
    private final List<BiConsumer<LevelChunk, ServerPlayer>> onChunkSent = new ArrayList<>();
    private final List<Consumer<ServerPlayer>> onPlayerJoined = new ArrayList<>();
    private final List<Consumer<ServerPlayer>> onDatapackSync = new ArrayList<>();

    private final IEventBus modBus;

    public NeoForgeChartaMod(IEventBus modBus) {
        this.modBus = modBus;
        this.init();
        if (FMLEnvironment.dist.isClient()) {
            loadPlatformClass(ChartaModClient.class);
        }
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> this.reloadListeners.forEach(event::addListener));
        NeoForge.EVENT_BUS.addListener(VillagerTradesEvent.class, event -> this.villagerTrades.forEach((entry, list) -> {
            if(entry.get() == event.getType()) list.forEach(p -> event.getTrades().get(p.getFirst()).add(p.getSecond().get()));
        }));
        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, event -> this.onServerAboutToStart.forEach(c -> c.accept(event.getServer())));
        NeoForge.EVENT_BUS.addListener(ChunkWatchEvent.Sent.class, event -> this.onChunkSent.forEach(c -> c.accept(event.getChunk(), event.getPlayer())));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> this.onPlayerJoined.forEach(c -> {
            if(event.getEntity() instanceof ServerPlayer player) c.accept(player);
        }));
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> this.onDatapackSync.forEach(c -> c.accept(event.getPlayer())));
    }

    @Override
    public String getPlatform() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    protected void registerReloadableListener(ResourceLocation identifier, PreparableReloadListener listener) {
        this.reloadListeners.add(listener);
    }

    @Override
    protected void addVillagerTrade(MinecraftEntry<VillagerProfession> profession, int level, Supplier<VillagerTrades.ItemListing> listing) {
        this.villagerTrades.computeIfAbsent(profession, p -> new ArrayList<>()).add(Pair.of(level, listing));
    }

    @Override
    public <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, BiFunction<Integer, Inventory, M> constructor, Player player, D data, Component title) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return constructor.apply(containerId, playerInventory);
            }
        }, buf -> entry.getStreamCodec().encode(buf, data));
    }

    @Override
    public void registerEventOnServerAboutToStart(Consumer<MinecraftServer> consumer) {
        this.onServerAboutToStart.add(consumer);
    }

    @Override
    public void registerEventOnChunkSent(BiConsumer<LevelChunk, ServerPlayer> consumer) {
        this.onChunkSent.add(consumer);
    }

    @Override
    public void registerEventOnPlayerJoined(Consumer<ServerPlayer> consumer) {
        this.onPlayerJoined.add(consumer);
    }

    @Override
    public void registerEventOnDatapackSync(Consumer<ServerPlayer> consumer) {
        this.onDatapackSync.add(consumer);
    }

    public static IEventBus getModBus() {
        return ((NeoForgeChartaMod) ChartaMod.getInstance()).modBus;
    }

}