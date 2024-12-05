package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;
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

import java.util.List;

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
                GameSlot carriedCards = cardMenu.getCarriedCards();
                if(carriedCards.isEmpty() && slot.canRemoveCard(cardPlayer, payload.cardId)) {
                    List<Card> cards = slot.removeCards(payload.cardId);
                    cardMenu.setCarriedCards(new GameSlot(cards));
                    slot.onRemove(cardPlayer, cards);
                }else if(!carriedCards.isEmpty() && slot.canInsertCard(cardPlayer, carriedCards.stream().toList(), payload.cardId) && slot.insertCards(carriedCards, payload.cardId)) {
                    cardMenu.setCarriedCards(new GameSlot());
                    slot.onInsert(cardPlayer, carriedCards.stream().toList());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
