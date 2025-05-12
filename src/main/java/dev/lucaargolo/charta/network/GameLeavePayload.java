package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.gui.screens.ConfirmScreen;
import dev.lucaargolo.charta.utils.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class GameLeavePayload implements CustomPacketPayload {

    public GameLeavePayload() {

    }

    public GameLeavePayload(FriendlyByteBuf buf) {

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    public static void handleBoth(GameLeavePayload payload, NetworkEvent.Context context) {
        if(context.getDirection().getReceptionSide().isServer()) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleServer(GameLeavePayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            context.getSender().stopRiding();
        });
    }

    public static void handleClient(GameLeavePayload payload, NetworkEvent.Context context) {
        context.enqueueWork(GameLeavePayload::openExitScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openExitScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(null, Component.translatable("message.charta.leaving_game"), true, () -> {
            if(minecraft.player != null) {
                minecraft.player.stopRiding();
                PacketUtils.sendToServer(new GameLeavePayload());
            }
        }));
    }


}
