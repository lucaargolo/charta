package dev.lucaargolo.hexedaces;

import dev.lucaargolo.hexedaces.block.ModBlocks;
import dev.lucaargolo.hexedaces.client.HexedAcesClient;
import dev.lucaargolo.hexedaces.client.screen.CardGameScreen;
import dev.lucaargolo.hexedaces.network.CardImagesPayload;
import dev.lucaargolo.hexedaces.resources.CardImageResource;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HexedAces.MOD_ID)
public class HexedAces {

    public static final String MOD_ID = "hexedaces";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CardImageResource CARD_IMAGES = new CardImageResource("card");
    public static final CardImageResource DECK_IMAGES = new CardImageResource("deck");

    public HexedAces(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HexedAces.MOD_ID, path);
    }

    @EventBusSubscriber(modid = HexedAces.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(CardImagesPayload.TYPE, CardImagesPayload.STREAM_CODEC, CardImagesPayload::handleClient);
        }

    }

    @EventBusSubscriber(modid = HexedAces.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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
                PacketDistributor.sendToPlayer(serverPlayer, new CardImagesPayload(HexedAces.CARD_IMAGES.getImages(), HexedAces.DECK_IMAGES.getImages()));
            }
        }

        @SubscribeEvent
        public static void onDatapackReload(OnDatapackSyncEvent event) {
            PacketDistributor.sendToAllPlayers(new CardImagesPayload(HexedAces.CARD_IMAGES.getImages(), HexedAces.DECK_IMAGES.getImages()));
        }

    }

}
