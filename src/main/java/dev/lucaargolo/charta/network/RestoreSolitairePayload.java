package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class RestoreSolitairePayload implements CustomPacketPayload {

    public RestoreSolitairePayload() {

    }

    public RestoreSolitairePayload(FriendlyByteBuf buf) {

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    public static void handleServer(RestoreSolitairePayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof SolitaireMenu solitaireMenu) {
                solitaireMenu.getGame().restore();
            }
        });
    }

}
