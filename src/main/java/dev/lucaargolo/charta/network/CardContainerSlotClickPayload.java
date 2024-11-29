package dev.lucaargolo.charta.network;

import com.google.common.collect.ImmutableList;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CardContainerSlotClickPayload(int containerId, int slotId, int cardId) implements CustomPacketPayload {

    public static final Type<CardContainerSlotClickPayload> TYPE = new Type<>(Charta.id("card_container_slot_click"));

    public static final StreamCodec<ByteBuf, CardContainerSlotClickPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::containerId,
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::slotId,
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::cardId,
            CardContainerSlotClickPayload::new
    );

    public static void handleServer(CardContainerSlotClickPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if(player instanceof LivingEntityMixed mixed && player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
                CardPlayer cardPlayer = mixed.charta_getCardPlayer();
                CardSlot<?> slot = cardMenu.getCardSlot(payload.slotId);
                ImmutableList<Card> carriedCards = cardMenu.getCarriedCards();
                if(carriedCards.isEmpty() && slot.canRemoveCard(cardPlayer)) {
                    Card lastCard = slot.removeCard(payload.cardId);
                    cardMenu.setCarriedCards(ImmutableList.of(lastCard));
                    slot.onRemove(cardPlayer, lastCard);
                }else if(!carriedCards.isEmpty() && slot.canInsertCard(cardPlayer, carriedCards) && slot.insertCards(carriedCards, payload.cardId)) {
                    cardMenu.setCarriedCards(ImmutableList.of());
                    for(Card card : carriedCards) {
                        slot.onInsert(cardPlayer, card);
                    }
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
