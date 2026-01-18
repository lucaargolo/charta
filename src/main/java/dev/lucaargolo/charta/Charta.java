package dev.lucaargolo.charta;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.datagen.ModChestLootProvider;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.entity.ModVillagerProfessions;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.game.Rank;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.DeckResource;
import dev.lucaargolo.charta.resources.SuitImageResource;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.slf4j.Logger;

import java.util.*;

@SuppressWarnings("unused")
public class Charta implements ModInitializer {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace( "empty"));
    public static final Set<Suit> DEFAULT_SUITS = Set.of(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS);
    public static final Set<Rank> DEFAULT_RANKS = Set.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING);

    public static final String MOD_ID = "charta";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Style SYMBOLS = Style.EMPTY.withFont(Charta.id("symbols"));
    public static final Style MINERCRAFTORY = Style.EMPTY.withFont(Charta.id("minercraftory"));

    public static final ResourceLocation MISSING_DECK = Charta.id("missing_deck");
    public static final ResourceLocation MISSING_SUIT = Charta.id("missing_suit");
    public static final ResourceLocation MISSING_CARD = Charta.id("missing_card");
    public static final ResourceLocation MISSING_GAME = Charta.id("missing_game");

    public static final SuitImageResource SUIT_IMAGES = new SuitImageResource();
    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");
    public static final DeckResource CARD_DECKS = new DeckResource();

    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModEntityTypes.register();
        ModPoiTypes.register();
        ModVillagerProfessions.register();
        ModBlockEntityTypes.register();
        ModMenus.register();
        ModCreativeTabs.register();
        ModDataComponentTypes.register();
        ModSounds.register();

        Charta.registerPayloads();
        Charta.registerEvents();
        Charta.registerLootModifiers();
    }

    private static void registerLootModifiers() {
        ModChestLootProvider chestLoot = new ModChestLootProvider(null);
        Map<ResourceKey<LootTable>, LootTable.Builder> builders = new LinkedHashMap<>();
        chestLoot.generate(builders::put);
        LootTableEvents.MODIFY.register((key, builder, source, provider) -> {
            if(source.isBuiltin()) {
                String id = key.location().toString();
                LootTable.Builder mod;
                if (id.equals("minecraft:chests/simple_dungeon")) {
                    mod = builders.get(ModChestLootProvider.SIMPLE_DUNGEON_DECKS);
                } else if (id.equals("minecraft:chests/desert_pyramid")) {
                    mod = builders.get(ModChestLootProvider.DESERT_PYRAMID_DECKS);
                } else if (id.equals("minecraft:chests/abandoned_mineshaft")) {
                    mod = builders.get(ModChestLootProvider.ABANDONED_MINESHAFT_DECKS);
                } else {
                    mod = null;
                }
                if(mod != null) {
                    List<LootPool> pools = mod.pools.build();
                    if(!pools.isEmpty()) {
                        Charta.LOGGER.info("Modifying {} with {} pools", key, pools.size());
                        builder.pools(pools);
                    }
                    List<LootItemFunction> functions = mod.functions.build();
                    if(!functions.isEmpty()) {
                        Charta.LOGGER.info("Modifying {} with {} functions", key, functions.size());
                        builder.apply(functions);
                    }
                }
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Charta.MOD_ID, path);
    }

    private static void registerPayloads() {
        registrarPlayToClient(ImagesPayload.TYPE, ImagesPayload.STREAM_CODEC, ImagesPayload::handleClient);
        registrarPlayToClient(CardDecksPayload.TYPE, CardDecksPayload.STREAM_CODEC, CardDecksPayload::handleClient);
        registrarPlayToClient(UpdateCardContainerSlotPayload.TYPE, UpdateCardContainerSlotPayload.STREAM_CODEC, UpdateCardContainerSlotPayload::handleClient);
        registrarPlayToClient(UpdateCardContainerCarriedPayload.TYPE, UpdateCardContainerCarriedPayload.STREAM_CODEC, UpdateCardContainerCarriedPayload::handleClient);
        registrarPlayToClient(TableScreenPayload.TYPE, TableScreenPayload.STREAM_CODEC, TableScreenPayload::handleClient);
        registrarPlayToClient(GameSlotCompletePayload.TYPE, GameSlotCompletePayload.STREAM_CODEC, GameSlotCompletePayload::handleClient);
        registrarPlayToClient(GameSlotPositionPayload.TYPE, GameSlotPositionPayload.STREAM_CODEC, GameSlotPositionPayload::handleClient);
        registrarPlayToClient(GameSlotResetPayload.TYPE, GameSlotResetPayload.STREAM_CODEC, GameSlotResetPayload::handleClient);
        registrarPlayToClient(GameStartPayload.TYPE, GameStartPayload.STREAM_CODEC, GameStartPayload::handleClient);
        registrarPlayToClient(CardPlayPayload.TYPE, CardPlayPayload.STREAM_CODEC, CardPlayPayload::handleClient);

        registrarPlayToServer(CardContainerSlotClickPayload.TYPE, CardContainerSlotClickPayload.STREAM_CODEC, CardContainerSlotClickPayload::handleServer);
        registrarPlayToServer(CardTableSelectGamePayload.TYPE, CardTableSelectGamePayload.STREAM_CODEC, CardTableSelectGamePayload::handleServer);
        registrarPlayToServer(RestoreSolitairePayload.TYPE, RestoreSolitairePayload.STREAM_CODEC, RestoreSolitairePayload::handleServer);

        registrarPlayBidirectional(LastFunPayload.TYPE, LastFunPayload.STREAM_CODEC, LastFunPayload::handleClient, LastFunPayload::handleServer);
        registrarPlayBidirectional(PlayerOptionsPayload.TYPE, PlayerOptionsPayload.STREAM_CODEC, PlayerOptionsPayload::handleClient, PlayerOptionsPayload::handleServer);
        registrarPlayBidirectional(GameLeavePayload.TYPE, GameLeavePayload.STREAM_CODEC, GameLeavePayload::handleClient, GameLeavePayload::handleServer);
    }

    private static <T extends CustomPacketPayload> void registrarPlayToClient(CustomPacketPayload.Type<T> type, StreamCodec<ByteBuf, T> codec, BiConsumer<Player, T> handler) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientReceiver(type, handler);
        }
    }

    @Environment(EnvType.CLIENT)
    private static <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> type, BiConsumer<Player, T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> ctx.client().execute(() -> handler.accept(ctx.player(), payload)));
    }

    private static <T extends CustomPacketPayload> void registrarPlayToServer(CustomPacketPayload.Type<T> type, StreamCodec<ByteBuf, T> codec, BiConsumer<Player, T> handler) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> ctx.server().execute(() -> handler.accept(ctx.player(), payload)));
    }

    private static <T extends CustomPacketPayload> void registrarPlayBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<ByteBuf, T> codec,BiConsumer<Player, T> clientHandler, BiConsumer<Player, T> serverHandler) {
        registrarPlayToClient(type, codec, clientHandler);
        registrarPlayToServer(type, codec, serverHandler);
    }

    private static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(GameEvents::serverAboutToStart);
        ServerPlayConnectionEvents.JOIN.register(GameEvents::onPlayerJoined);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(GameEvents::onDatapackReload);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((a, b) -> ModVillagerProfessions.registerVillagerTrades());
        GameEvents.addReloadListeners(ResourceManagerHelper.get(PackType.SERVER_DATA));
    }

    public static class GameEvents {

        public static void serverAboutToStart(MinecraftServer server) {
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

        public static void addReloadListeners(ResourceManagerHelper event) {
            event.registerReloadListener(SUIT_IMAGES);
            event.registerReloadListener(CARD_IMAGES);
            event.registerReloadListener(DECK_IMAGES);
            event.registerReloadListener(CARD_DECKS);
        }

        public static void onChunkSent(ServerPlayer player, LevelChunk chunk) {
            chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                if(blockEntity instanceof CardTableBlockEntity cardTable) {
                    int count = cardTable.getSlotCount();
                    for(int i = 0; i < count; i++) {
                        GameSlot slot = cardTable.getSlot(i);
                        GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
                        ServerPlayNetworking.send(player, payload);
                    }
                }
            });

        }

        public static void onPlayerJoined(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
            ServerPlayer serverPlayer = handler.getPlayer();
            ServerPlayNetworking.send(serverPlayer, new ImagesPayload(
                    new HashMap<>(Charta.SUIT_IMAGES.getImages()),
                    new HashMap<>(Charta.CARD_IMAGES.getImages()),
                    new HashMap<>(Charta.DECK_IMAGES.getImages())
            ));
            ServerPlayNetworking.send(serverPlayer, new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
            PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData.factory(), "charta_player_options");
            ServerPlayNetworking.send(serverPlayer, new PlayerOptionsPayload(data.getPlayerOptions(serverPlayer)));

        }

        public static void onDatapackReload(ServerPlayer player, boolean joined) {
            ServerPlayNetworking.send(player, new ImagesPayload(
                new HashMap<>(Charta.SUIT_IMAGES.getImages()),
                new HashMap<>(Charta.CARD_IMAGES.getImages()),
                new HashMap<>(Charta.DECK_IMAGES.getImages())
            ));
            ServerPlayNetworking.send(player, new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
        }

    }

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
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

}
