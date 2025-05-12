package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class GameStartPayload implements CustomPacketPayload {

    public GameStartPayload() {

    }

    public GameStartPayload(FriendlyByteBuf buf) {

    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

    public static void handleClient(GameStartPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(GameStartPayload::onGameStart);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onGameStart() {
        ChartaClient.LOCAL_HISTORY.clear();
    }

}
