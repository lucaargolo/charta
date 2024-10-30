package dev.lucaargolo.charta;

import com.mojang.logging.LogUtils;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.network.CardImagesPayload;
import dev.lucaargolo.charta.resources.CardImageResource;
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

    public static final String MOD_ID = "charta";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");

    public static EntityDataAccessor<List<Card>> DATA_CHARTA_HAND;

    public Charta(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModEntityDataSerializers.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Charta.MOD_ID, path);
    }

    @EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(CardImagesPayload.TYPE, CardImagesPayload.STREAM_CODEC, CardImagesPayload::handleClient);
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
