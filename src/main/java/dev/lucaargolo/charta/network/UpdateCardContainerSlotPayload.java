package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class UpdateCardContainerSlotPayload implements CustomPacketPayload {

    private final int containerId;
    private final int stateId;
    private final int slotId;
    private final List<Card> cards;

    public UpdateCardContainerSlotPayload(int containerId, int stateId, int slotId, List<Card> cards) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.slotId = slotId;
        this.cards = cards;
    }

    public UpdateCardContainerSlotPayload(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.stateId = buf.readInt();
        this.slotId = buf.readInt();
        int size = buf.readInt();
        this.cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.cards.add(Card.fromBuf(buf));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(stateId);
        buf.writeInt(slotId);
        buf.writeInt(cards.size());
        for (Card card : cards) {
            card.toBuf(buf);
        }
    }

    public static void handleClient(UpdateCardContainerSlotPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> updateCardContainerSlot(payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateCardContainerSlot(UpdateCardContainerSlotPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        assert player != null;
        if(player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
            cardMenu.setCards(payload.slotId, payload.stateId, payload.cards);
        }
    }

}
