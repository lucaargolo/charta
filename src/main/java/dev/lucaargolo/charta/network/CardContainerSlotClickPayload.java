package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class CardContainerSlotClickPayload implements CustomPacketPayload {

    private final int containerId;
    private final int slotId;
    private final int cardId;

    public CardContainerSlotClickPayload(int containerId, int slotId, int cardId) {
        this.containerId = containerId;
        this.slotId = slotId;
        this.cardId = cardId;
    }

    public CardContainerSlotClickPayload(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.slotId = buf.readInt();
        this.cardId = buf.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(slotId);
        buf.writeInt(cardId);
    }

    public static void handleServer(CardContainerSlotClickPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if(player instanceof LivingEntityMixed mixed && player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
                CardPlayer cardPlayer = mixed.charta_getCardPlayer();
                CardSlot<?> slot = cardMenu.getCardSlot(payload.slotId);
                GameSlot carriedCards = cardMenu.getCarriedCards();
                if(carriedCards.isEmpty() && slot.canRemoveCard(cardPlayer, payload.cardId)) {
                    slot.preUpdate();
                    List<Card> cards = slot.removeCards(payload.cardId);
                    cardMenu.setCarriedCards(new GameSlot(cards));
                    slot.onRemove(cardPlayer, cards);
                    slot.postUpdate();
                }else if(!carriedCards.isEmpty() && slot.canInsertCard(cardPlayer, carriedCards.stream().toList(), payload.cardId) && slot.insertCards(carriedCards, payload.cardId)) {
                    slot.preUpdate();
                    cardMenu.setCarriedCards(new GameSlot());
                    slot.onInsert(cardPlayer, carriedCards.stream().toList());
                    slot.postUpdate();
                }
            }
        });
        context.setPacketHandled(true);
    }

}
