package dev.lucaargolo.charta;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBannerPatterns;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.entity.ModVillagerProfessions;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.loot.ModLootModifiers;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.resources.CardDeckResource;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.CardSuitResource;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.PacketUtils;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Style;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod(Charta.MOD_ID)
@SuppressWarnings("unused")
public class Charta {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation("empty"));

    public static final String MOD_ID = "charta";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Style SYMBOLS = Style.EMPTY.withFont(Charta.id("symbols"));
    public static final Style MINERCRAFTORY = Style.EMPTY.withFont(Charta.id("minercraftory"));

    public static final ResourceLocation MISSING_DECK = Charta.id("missing_deck");
    public static final ResourceLocation MISSING_SUIT = Charta.id("missing_suit");
    public static final ResourceLocation MISSING_CARD = Charta.id("missing_card");
    public static final ResourceLocation MISSING_GAME = Charta.id("missing_game");

    public static final CardSuitResource CARD_SUITS = new CardSuitResource();
    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");
    public static final CardDeckResource CARD_DECKS = new CardDeckResource();

    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    private static final String PROTOCOL = "1";
    public static SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(id("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static int id = 0;


    public Charta() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBannerPatterns.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        ModVillagerProfessions.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        Charta.registerPayloads();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Charta.MOD_ID, path);
    }

    private static int id() {
        return id++;
    }

    public static void registerPayloads() {

        registrarPlayToClient(ImagesPayload.class, ImagesPayload::new, ImagesPayload::handleClient);
        registrarPlayToClient(CardDecksPayload.class, CardDecksPayload::new, CardDecksPayload::handleClient);
        registrarPlayToClient(UpdateCardContainerSlotPayload.class, UpdateCardContainerSlotPayload::new, UpdateCardContainerSlotPayload::handleClient);
        registrarPlayToClient(UpdateCardContainerCarriedPayload.class, UpdateCardContainerCarriedPayload::new, UpdateCardContainerCarriedPayload::handleClient);
        registrarPlayToClient(TableScreenPayload.class, TableScreenPayload::new, TableScreenPayload::handleClient);
        registrarPlayToClient(GameSlotCompletePayload.class, GameSlotCompletePayload::new, GameSlotCompletePayload::handleClient);
        registrarPlayToClient(GameSlotPositionPayload.class, GameSlotPositionPayload::new, GameSlotPositionPayload::handleClient);
        registrarPlayToClient(GameSlotResetPayload.class, GameSlotResetPayload::new, GameSlotResetPayload::handleClient);
        registrarPlayToClient(GameStartPayload.class, GameStartPayload::new, GameStartPayload::handleClient);
        registrarPlayToClient(CardPlayPayload.class, CardPlayPayload::new, CardPlayPayload::handleClient);

        registrarPlayToServer(CardContainerSlotClickPayload.class, CardContainerSlotClickPayload::new, CardContainerSlotClickPayload::handleServer);
        registrarPlayToServer(CardTableSelectGamePayload.class, CardTableSelectGamePayload::new, CardTableSelectGamePayload::handleServer);
        registrarPlayToServer(RestoreSolitairePayload.class, RestoreSolitairePayload::new, RestoreSolitairePayload::handleServer);

        registrarPlayBidirectional(LastFunPayload.class, LastFunPayload::new, LastFunPayload::handleBoth);
        registrarPlayBidirectional(PlayerOptionsPayload.class, PlayerOptionsPayload::new, PlayerOptionsPayload::handleBoth);
        registrarPlayBidirectional(GameLeavePayload.class, GameLeavePayload::new, GameLeavePayload::handleBoth);

    }

    private static <T extends CustomPacketPayload> void registrarPlayToClient(Class<T> type, Function<FriendlyByteBuf, T> codec, BiConsumer<T, NetworkEvent.Context> handler) {
        NETWORK.messageBuilder(type, id())
            .encoder(CustomPacketPayload::toBytes)
            .decoder(codec)
            .consumerMainThread((payload, supplier) -> {
                NetworkEvent.Context context = supplier.get();
                if(context.getDirection().getReceptionSide().isClient()) {
                    handler.accept(payload, context);
                }
            })
            .add();
    }

    private static <T extends CustomPacketPayload> void registrarPlayToServer(Class<T> type, Function<FriendlyByteBuf, T> codec, BiConsumer<T, NetworkEvent.Context> handler) {
        NETWORK.messageBuilder(type, id())
            .encoder(CustomPacketPayload::toBytes)
            .decoder(codec)
            .consumerMainThread((payload, supplier) -> {
                NetworkEvent.Context context = supplier.get();
                if(context.getDirection().getReceptionSide().isServer()) {
                    handler.accept(payload, context);
                }
            })
            .add();
    }

    private static <T extends CustomPacketPayload> void registrarPlayBidirectional(Class<T> type, Function<FriendlyByteBuf, T> codec, BiConsumer<T, NetworkEvent.Context> handler) {
        NETWORK.messageBuilder(type, id())
            .encoder(CustomPacketPayload::toBytes)
            .decoder(codec)
            .consumerMainThread((payload, supplier) -> {
                handler.accept(payload, supplier.get());
            })
            .add();
    }


    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
    public static class GameEvents {

        @SubscribeEvent
        public static void serverAboutToStart(final ServerAboutToStartEvent event) {
            MinecraftServer server = event.getServer();
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

        @SubscribeEvent
        public static void addReloadListeners(final AddReloadListenerEvent event) {
            event.addListener(CARD_SUITS);
            event.addListener(CARD_IMAGES);
            event.addListener(DECK_IMAGES);
            event.addListener(CARD_DECKS);
        }

        @SubscribeEvent
        public static void onChunkSent(final ChunkWatchEvent.Watch event) {
            LevelChunk chunk = event.getChunk();
            chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                if(blockEntity instanceof CardTableBlockEntity cardTable) {
                    int count = cardTable.getSlotCount();
                    for(int i = 0; i < count; i++) {
                        GameSlot slot = cardTable.getSlot(i);
                        GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
                        PacketUtils.sendToPlayer(event.getPlayer(), payload);
                    }
                }
            });

        }

        @SubscribeEvent
        public static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if(player instanceof ServerPlayer serverPlayer) {
                PacketUtils.sendToPlayer(serverPlayer, new ImagesPayload(
                    new HashMap<>(Charta.CARD_SUITS.getImages()),
                    new HashMap<>(Charta.CARD_IMAGES.getImages()),
                    new HashMap<>(Charta.DECK_IMAGES.getImages())
                ));
                PacketUtils.sendToPlayer(serverPlayer, new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
                PlayerOptionData data = serverPlayer.server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData::load, PlayerOptionData::new, "charta_player_options");
                PacketUtils.sendToPlayer(serverPlayer, new PlayerOptionsPayload(data.getPlayerOptions(serverPlayer)));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(final OnDatapackSyncEvent event) {
            PacketUtils.sendToAllPlayers(new ImagesPayload(
                new HashMap<>(Charta.CARD_SUITS.getImages()),
                new HashMap<>(Charta.CARD_IMAGES.getImages()),
                new HashMap<>(Charta.DECK_IMAGES.getImages())
            ));
            PacketUtils.sendToAllPlayers(new CardDecksPayload(new LinkedHashMap<>(Charta.CARD_DECKS.getDecks())));
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
