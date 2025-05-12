package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedList;
import java.util.List;

public class UpdateCardContainerCarriedPayload implements CustomPacketPayload {

    private final int containerId;
    private final int stateId;
    private final List<Card> cards;

    public UpdateCardContainerCarriedPayload(int containerId, int stateId, List<Card> cards) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.cards = cards;
    }

    public UpdateCardContainerCarriedPayload(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.stateId = buf.readInt();
        this.cards = new LinkedList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.cards.add(Card.fromBuf(buf));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(stateId);
        buf.writeInt(cards.size());
        for (Card card : cards) {
            card.toBuf(buf);
        }
    }

    public static void handleClient(UpdateCardContainerCarriedPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if(player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
                cardMenu.setCarriedCards(payload.stateId, new GameSlot(payload.cards));
            }
        });
    }

}
