package dev.lucaargolo.charta;

import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.network.CardContainerSlotClickPayload;
import dev.lucaargolo.charta.network.CardImagesPayload;
import dev.lucaargolo.charta.network.UpdateCardContainerCarriedPayload;
import dev.lucaargolo.charta.network.UpdateCardContainerSlotPayload;
import dev.lucaargolo.charta.resources.CardImageResource;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
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
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.List;

@Mod(Charta.MOD_ID)
public class Charta {

    /*
    TODO:
        Modfest Goals:
            - Make wooden table, you can merge it like furniture tables, but only some formats are valid (3x3, 5x3, 7x3, etc)
            - Make table cloth, you can only put it on top of a valid table. Putting the table cloth makes it able to play games.
            - Make wooden stool, you can only place it next to valid tables, in predefined spots (3x3 = 4 players, 5x3 = 6 players, etc)
            - Implement generic game interface on table screen (maybe like a simple screen menu, but instead of slots we would have card lists, etc)
            - Implement generic game loop on table block entity
            - Implement game visualization on table block entity renderer
            - Populate CardPlayer play methods for players (will wait for player input) and for villagers (will wait an amount of time and select a play)
            - Make players able to start games by sitting on the stools
            - Make villagers able to join games by sitting on the stools
            - Make villagers able to randomly starts game
            - Make game bar structure that can spawn in villages
            - Make card seller that sells regular card decks
            - Make some custom card decks spawn in dungeons
            - Make card and deck items inventory
                - Make the deck be like a bundle with individual cards inside it.
                - Players can make custom decks by mixing cards inside the bundles
                - A deck  needs to have all cards to be able to start a game
            - Add deck to table which changes the card designs
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

    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");

    public static EntityDataAccessor<List<Card>> PLAYER_HAND;
    public static EntityDataAccessor<List<Card>> VILLAGER_HAND;

    public Charta(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityDataSerializers.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModMenus.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Charta.MOD_ID, path);
    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            if(event.getRegistry() == NeoForgeRegistries.ENTITY_DATA_SERIALIZERS) {
                PLAYER_HAND = SynchedEntityData.defineId(Player.class, ModEntityDataSerializers.CARD_LIST.get());
                VILLAGER_HAND = SynchedEntityData.defineId(Villager.class, ModEntityDataSerializers.CARD_LIST.get());
            }
        }

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");

            registrar.playToClient(CardImagesPayload.TYPE, CardImagesPayload.STREAM_CODEC, CardImagesPayload::handleClient);
            registrar.playToClient(UpdateCardContainerSlotPayload.TYPE, UpdateCardContainerSlotPayload.STREAM_CODEC, UpdateCardContainerSlotPayload::handleClient);
            registrar.playToClient(UpdateCardContainerCarriedPayload.TYPE, UpdateCardContainerCarriedPayload.STREAM_CODEC, UpdateCardContainerCarriedPayload::handleClient);

            registrar.playToServer(CardContainerSlotClickPayload.TYPE, CardContainerSlotClickPayload.STREAM_CODEC, CardContainerSlotClickPayload::handleServer);
        }

    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(CARD_IMAGES);
            event.addListener(DECK_IMAGES);
        }

        @SubscribeEvent
        public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            if(player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new CardImagesPayload(Charta.CARD_IMAGES.getImages(), Charta.DECK_IMAGES.getImages()));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(OnDatapackSyncEvent event) {
            PacketDistributor.sendToAllPlayers(new CardImagesPayload(Charta.CARD_IMAGES.getImages(), Charta.DECK_IMAGES.getImages()));
        }

    }

}
