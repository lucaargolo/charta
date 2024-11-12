package dev.lucaargolo.charta;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.entity.ModPoiTypes;
import dev.lucaargolo.charta.entity.ModVillagerProfessions;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.resources.CardDeckResource;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.CardSuitResource;
import dev.lucaargolo.charta.utils.GameSlot;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod(Charta.MOD_ID)
public class Charta {

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace( "empty"));

    /*
    TODO:
        Release Goals:
            - Fix the order of the players to make sense.
            - Improve game screen (sound effects and if possible animate plays) (hovered cards)
            - Make some custom card decks spawn in dungeons
            - Add other card games:
                - Solitaire
                - ?
        Future Goals:
            - Add card painter so players can make new cards on the go. (They'll be stored in a PersistentData instead of the datapack)
            - Add other games
                - Add domino
                - Add checkers
                - Add chess
                - Add tic tac toe
            - Add fortune seer villager
            - Add tarot packs that you can buy or find
            - Add fortune system (kinda like enchantments for players)
                - You get a fortune by opening a tarot pack
                - If you want to remove a fortune you have to do a cleansing ritual
            - Add cleansing ritual

     */

    public static final String MOD_ID = "charta";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation MISSING_SUIT = Charta.id("missing_suit");
    public static final ResourceLocation MISSING_CARD = Charta.id("missing_card");

    public static final CardSuitResource CARD_SUITS = new CardSuitResource();
    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");
    public static final CardDeckResource CARD_DECKS = new CardDeckResource();

    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    public Charta(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        ModVillagerProfessions.register(modEventBus);
        ModBlockEntityTypes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataComponentTypes.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Charta.MOD_ID, path);
    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(ImagesPayload.TYPE, ImagesPayload.STREAM_CODEC, ImagesPayload::handleClient);
            registrar.playToClient(CardDecksPayload.TYPE, CardDecksPayload.STREAM_CODEC, CardDecksPayload::handleClient);
            registrar.playToClient(UpdateCardContainerSlotPayload.TYPE, UpdateCardContainerSlotPayload.STREAM_CODEC, UpdateCardContainerSlotPayload::handleClient);
            registrar.playToClient(UpdateCardContainerCarriedPayload.TYPE, UpdateCardContainerCarriedPayload.STREAM_CODEC, UpdateCardContainerCarriedPayload::handleClient);
            registrar.playToClient(OpenCardTableScreenPayload.TYPE, OpenCardTableScreenPayload.STREAM_CODEC, OpenCardTableScreenPayload::handleClient);
            registrar.playToClient(GameSlotCompletePayload.TYPE, GameSlotCompletePayload.STREAM_CODEC, GameSlotCompletePayload::handleClient);
            registrar.playToClient(GameSlotPositionPayload.TYPE, GameSlotPositionPayload.STREAM_CODEC, GameSlotPositionPayload::handleClient);
            registrar.playToClient(GameSlotResetPayload.TYPE, GameSlotResetPayload.STREAM_CODEC, GameSlotResetPayload::handleClient);

            registrar.playToServer(CardContainerSlotClickPayload.TYPE, CardContainerSlotClickPayload.STREAM_CODEC, CardContainerSlotClickPayload::handleServer);
            registrar.playToServer(CardTableSelectGamePayload.TYPE, CardTableSelectGamePayload.STREAM_CODEC, CardTableSelectGamePayload::handleServer);
        }

    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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
                    "charta:taiga_card_bar", 50);

            addBuildingToPool(templatePoolRegistry, processorListRegistry,
                    ResourceLocation.tryParse("minecraft:village/savanna/houses"),
                    "charta:savanna_card_bar", 50);
        }

        @SubscribeEvent
        public static void addReloadListeners(final AddReloadListenerEvent event) {
            event.addListener(CARD_SUITS);
            event.addListener(CARD_IMAGES);
            event.addListener(DECK_IMAGES);
            event.addListener(CARD_DECKS);
        }

        @SubscribeEvent
        public static void onChunkSent(final ChunkWatchEvent.Sent event) {
            LevelChunk chunk = event.getChunk();
            chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                if(blockEntity instanceof CardTableBlockEntity cardTable) {
                    int count = cardTable.getGameSlotCount();
                    for(int i = 0; i < count; i++) {
                        GameSlot slot = cardTable.getGameSlot(i);
                        GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
                        PacketDistributor.sendToPlayer(event.getPlayer(), payload);
                    }
                }
            });

        }

        @SubscribeEvent
        public static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if(player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new ImagesPayload(
                    new HashMap<>(Charta.CARD_SUITS.getImages()),
                    new HashMap<>(Charta.CARD_IMAGES.getImages()),
                    new HashMap<>(Charta.DECK_IMAGES.getImages())
                ));
                PacketDistributor.sendToPlayer(serverPlayer, new CardDecksPayload(Charta.CARD_DECKS.getDecks()));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(final OnDatapackSyncEvent event) {
            PacketDistributor.sendToAllPlayers(new ImagesPayload(
                new HashMap<>(Charta.CARD_SUITS.getImages()),
                new HashMap<>(Charta.CARD_IMAGES.getImages()),
                new HashMap<>(Charta.DECK_IMAGES.getImages())
            ));
            PacketDistributor.sendToAllPlayers(new CardDecksPayload(Charta.CARD_DECKS.getDecks()));
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
