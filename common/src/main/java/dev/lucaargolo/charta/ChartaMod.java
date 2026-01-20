package dev.lucaargolo.charta;

import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.block.entity.CardTableBlockEntity;
import dev.lucaargolo.charta.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.entity.ModItemListings;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.entity.ModVillagerProfessions;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.game.Rank;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenuTypes;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.registry.*;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.DeckResource;
import dev.lucaargolo.charta.resources.SuitImageResource;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class ChartaMod {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace( "empty"));
    public static final Set<Suit> DEFAULT_SUITS = Set.of(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS);
    public static final Set<Rank> DEFAULT_RANKS = Set.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING);

    public static final String MOD_ID = "charta";
    public static final String MOD_NAME = "Charta";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Style SYMBOLS = Style.EMPTY.withFont(ChartaMod.id("symbols"));
    public static final Style MINERCRAFTORY = Style.EMPTY.withFont(ChartaMod.id("minercraftory"));

    public static final ResourceLocation MISSING_DECK = ChartaMod.id("missing_deck");
    public static final ResourceLocation MISSING_SUIT = ChartaMod.id("missing_suit");
    public static final ResourceLocation MISSING_CARD = ChartaMod.id("missing_card");
    public static final ResourceLocation MISSING_GAME = ChartaMod.id("missing_game");

    public static final SuitImageResource SUIT_IMAGES = new SuitImageResource();
    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");
    public static final DeckResource CARD_DECKS = new DeckResource();

    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    private static ChartaMod instance;

    private final ModPacketManager packetManager;

    public ChartaMod() {
        instance = this;
        this.packetManager = loadPlatformClass(ModPacketManager.class);
    }

    public final void init() {
        ModBlocks.REGISTRY.init();
        ModItems.REGISTRY.init();
        ModEntityTypes.REGISTRY.init();
        ModPoiTypes.REGISTRY.init();
        ModVillagerProfessions.REGISTRY.init();
        ModBlockEntityTypes.REGISTRY.init();
        ModMenuTypes.REGISTRY.init();
        ModCreativeTabs.REGISTRY.init();
        ModDataComponentTypes.REGISTRY.init();
        ModSounds.REGISTRY.init();
        this.registerReloadableListener(SUIT_IMAGES);
        this.registerReloadableListener(CARD_IMAGES);
        this.registerReloadableListener(DECK_IMAGES);
        this.registerReloadableListener(CARD_DECKS);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 1, ModItemListings.COMMON_DECKS);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 1, ModItemListings.DRINKS);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 2, ModItemListings.UNCOMMON_DECKS);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 2, ModItemListings.IRON_LEAD);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 3, ModItemListings.RARE_DECKS);
        this.addVillagerTrade(ModVillagerProfessions.DEALER, 4, ModItemListings.EPIC_DECKS);
    }

    public abstract String getPlatform();

    public abstract boolean isModLoaded(String modId);

    public abstract boolean isFakePlayer(Player player);

    protected abstract void registerReloadableListener(PreparableReloadListener listener);

    protected abstract void addVillagerTrade(MinecraftEntry<VillagerProfession> profession, int level, VillagerTrades.ItemListing listing);

    public abstract <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, BiFunction<Integer, Inventory, M> constructor, Player player, D data, Component title);

    public <M extends AbstractContainerMenu, D> void openMenu(ModMenuTypeRegistry.AdvancedMenuTypeEntry<M, D> entry, QuadFunction<Integer, Inventory, Container, D, M> constructor, Player player, Container container, D data, Component title) {
        this.openMenu(entry, (syncId, inventory) -> constructor.apply(syncId, inventory, container, data), player, data, title);
    }

    public <M extends AbstractContainerMenu> void openMenu(TriFunction<Integer, Inventory, Container, M> constructor, Player player, Container container, Component title) {
        this.openMenu((syncId, inventory) -> constructor.apply(syncId, inventory, container), player, title);
    }

    public <M extends AbstractContainerMenu> void openMenu(BiFunction<Integer, Inventory, M> constructor, Player player, Component title) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return constructor.apply(containerId, playerInventory);
            }
        });
    };

    public final void onServerAboutToStart(MinecraftServer server) {
        RegistryAccess registryAccess = server.registryAccess();
        Registry<StructureTemplatePool> templatePoolRegistry = registryAccess.registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = registryAccess.registry(Registries.PROCESSOR_LIST).orElseThrow();

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.tryParse("minecraft:village/plains/houses"),
                "charta:plains_card_bar", 50);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.tryParse("minecraft:village/desert/houses"),
                "charta:desert_card_bar", 50);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.tryParse("minecraft:village/taiga/houses"),
                "charta:taiga_card_bar", 40);

        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                ResourceLocation.tryParse("minecraft:village/savanna/houses"),
                "charta:savanna_card_bar", 60);
    }

    public final void onChunkSent(LevelChunk chunk, ServerPlayer player) {
        chunk.getBlockEntities().forEach((pos, blockEntity) -> {
            if(blockEntity instanceof CardTableBlockEntity cardTable) {
                int count = cardTable.getSlotCount();
                for(int i = 0; i < count; i++) {
                    GameSlot slot = cardTable.getSlot(i);
                    GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
                    this.packetManager.sendToPlayer(player, payload);
                }
            }
        });
    }

    public final void onPlayerJoined(ServerPlayer player) {
        this.packetManager.sendToPlayer(player, new ImagesPayload(
                new HashMap<>(ChartaMod.SUIT_IMAGES.getImages()),
                new HashMap<>(ChartaMod.CARD_IMAGES.getImages()),
                new HashMap<>(ChartaMod.DECK_IMAGES.getImages())
        ));
        this.packetManager.sendToPlayer(player, new CardDecksPayload(new LinkedHashMap<>(ChartaMod.CARD_DECKS.getDecks())));
        PlayerOptionData data = player.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData.factory(), "charta_player_options");
        this.packetManager.sendToPlayer(player, new PlayerOptionsPayload(data.getPlayerOptions(player)));
    }

    public final void onDatapackReload(MinecraftServer server) {
        this.packetManager.sendToAllPlayers(server, new ImagesPayload(
                new HashMap<>(ChartaMod.SUIT_IMAGES.getImages()),
                new HashMap<>(ChartaMod.CARD_IMAGES.getImages()),
                new HashMap<>(ChartaMod.DECK_IMAGES.getImages())
        ));
        this.packetManager.sendToAllPlayers(server, new CardDecksPayload(new LinkedHashMap<>(ChartaMod.CARD_DECKS.getDecks())));
    }

    private void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static ChartaMod getInstance() {
        return instance;
    }

    public static ModPacketManager getPacketManager() {
        return instance.packetManager;
    }

    public static <T> ModRegistry<T> registry(ResourceKey<Registry<T>> registryKey) {
        return loadPlatformClass(ModRegistry.class, registryKey);
    }

    public static ModBlockRegistry blockRegistry() {
        return loadPlatformClass(ModBlockRegistry.class);
    }

    public static ModBlockEntityTypeRegistry blockEntityTypeRegistry() {
        return loadPlatformClass(ModBlockEntityTypeRegistry.class);
    }

    public static ModItemRegistry itemRegistry() {
        return loadPlatformClass(ModItemRegistry.class);
    }

    public static ModMenuTypeRegistry menuTypeRegistry() { return loadPlatformClass(ModMenuTypeRegistry.class); }

    public static <T> T loadPlatformClass(Class<T> clazz, Object... parameters) {
        return loadPlatformClass(null, clazz, parameters);
    }

    public static <T> T loadPlatformClass(String mod, Class<T> clazz, Object... parameters) {
        String originalName = clazz.getName();
        String clazzPrefix = mod == null ? instance.getPlatform() : instance.isModLoaded(mod) ? instance.getPlatform() : "Empty";
        String clazzName = originalName.substring(0, originalName.lastIndexOf('.')) + "." + clazzPrefix + originalName.substring(originalName.lastIndexOf('.') + 1);
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        try {
            return (T) clazz.getClassLoader().loadClass(clazzName).getConstructor(parameterTypes).newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @FunctionalInterface
    public interface HexaFunction<P1, P2, P3, P4, P5, P6, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    @FunctionalInterface
    public interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    @FunctionalInterface
    public interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    @FunctionalInterface
    public interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

}
