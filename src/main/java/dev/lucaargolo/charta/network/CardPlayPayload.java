package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.client.gui.screens.HistoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class CardPlayPayload implements CustomPacketPayload {

    private final Component playerName;
    private final int playerCards;
    private final Component play;

    public CardPlayPayload(Component playerName, int playerCards, Component play) {
        this.playerName = playerName;
        this.playerCards = playerCards;
        this.play = play;
    }

    public CardPlayPayload(FriendlyByteBuf buf) {
        this.playerName = buf.readComponent();
        this.playerCards = buf.readInt();
        this.play = buf.readComponent();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeComponent(playerName);
        buf.writeInt(playerCards);
        buf.writeComponent(play);
    }

    public static void handleClient(CardPlayPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            addToHistory(payload.playerName, payload.playerCards, payload.play);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void addToHistory(Component playerName, int playerCards, Component play) {
        ChartaClient.LOCAL_HISTORY.add(ImmutableTriple.of(playerName, playerCards, play));
        Minecraft mc = Minecraft.getInstance();
        if(mc.screen instanceof HistoryScreen screen) {
            screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }
    }


}
