package dev.lucaargolo.charta;

import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBannerPatterns;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.ModEntityTypes;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.item.ModCreativeTabs;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.*;
import dev.lucaargolo.charta.resources.CardDeckResource;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.resources.CardSuitResource;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.List;

@Mod(Charta.MOD_ID)
public class Charta {

    /*
    TODO:
        Modfest Goals:
            - Implement game visualization on table block entity renderer
            - Make villagers able to randomly starts game
            - Make game bar structure that can spawn in villages
            - Make card seller that sells regular card decks
            - Make some custom card decks spawn in dungeons
            - Add other card games:
                - Solitaire
                - ?
        Other Goals:
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

    public static EntityDataAccessor<List<Card>> ENTITY_HAND;
    public static EntityDataAccessor<Boolean> MOB_IRON_LEASH;

    public Charta(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityDataSerializers.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
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

            registrar.playToServer(CardContainerSlotClickPayload.TYPE, CardContainerSlotClickPayload.STREAM_CODEC, CardContainerSlotClickPayload::handleServer);
            registrar.playToServer(CardTableSelectGamePayload.TYPE, CardTableSelectGamePayload.STREAM_CODEC, CardTableSelectGamePayload::handleServer);
        }

    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(CARD_SUITS);
            event.addListener(CARD_IMAGES);
            event.addListener(DECK_IMAGES);
            event.addListener(CARD_DECKS);
        }

        @SubscribeEvent
        public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if(player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new ImagesPayload(Charta.CARD_SUITS.getImages(), Charta.CARD_IMAGES.getImages(), Charta.DECK_IMAGES.getImages()));
                PacketDistributor.sendToPlayer(serverPlayer, new CardDecksPayload(Charta.CARD_DECKS.getDecks()));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(OnDatapackSyncEvent event) {
            PacketDistributor.sendToAllPlayers(new ImagesPayload(Charta.CARD_SUITS.getImages(), Charta.CARD_IMAGES.getImages(), Charta.DECK_IMAGES.getImages()));
            PacketDistributor.sendToAllPlayers(new CardDecksPayload(Charta.CARD_DECKS.getDecks()));
        }

    }

}
