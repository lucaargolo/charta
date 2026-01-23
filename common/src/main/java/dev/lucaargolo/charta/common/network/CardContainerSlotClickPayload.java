package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.CardPlayer;
import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.menu.CardSlot;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executor;

public record CardContainerSlotClickPayload(int containerId, int slotId, int cardId) implements CustomPacketPayload {

    public static final Type<CardContainerSlotClickPayload> TYPE = new Type<>(ChartaMod.id("card_container_slot_click"));

    public static final StreamCodec<ByteBuf, CardContainerSlotClickPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::containerId,
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::slotId,
            ByteBufCodecs.INT,
            CardContainerSlotClickPayload::cardId,
            CardContainerSlotClickPayload::new
    );

    public static void handleServer(CardContainerSlotClickPayload payload, ServerPlayer player, Executor executor) {
        executor.execute(() -> {
            if(player instanceof LivingEntityMixed mixed && player.containerMenu instanceof AbstractCardMenu<?, ?> cardMenu && cardMenu.containerId == payload.containerId) {
                CardPlayer cardPlayer = mixed.charta_getCardPlayer();
                CardSlot<?, ?> slot = cardMenu.getCardSlot(payload.slotId);
                GameSlot carriedCards = cardMenu.getCarriedCards();
                if(carriedCards.isEmpty() && slot.canRemoveCard(cardPlayer, payload.cardId)) {
                    slot.preUpdate();
                    List<Card> cards = slot.removeCards(payload.cardId);
                    cardMenu.setCarriedCards(new GameSlot(cards));
                    slot.onRemove(cardPlayer, cards, payload.cardId);
                    slot.postUpdate();
                }else if(!carriedCards.isEmpty() && slot.canInsertCard(cardPlayer, carriedCards.stream().toList(), payload.cardId) && slot.insertCards(carriedCards, payload.cardId)) {
                    slot.preUpdate();
                    cardMenu.setCarriedCards(new GameSlot());
                    slot.onInsert(cardPlayer, carriedCards.stream().toList(), payload.cardId);
                    slot.postUpdate();
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
