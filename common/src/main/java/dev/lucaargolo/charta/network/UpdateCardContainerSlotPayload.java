package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executor;

public record UpdateCardContainerSlotPayload(int containerId, int stateId, int slotId, List<Card> cards) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateCardContainerSlotPayload> TYPE = new CustomPacketPayload.Type<>(ChartaMod.id("update_card_container_slot"));

    public static final StreamCodec<ByteBuf, UpdateCardContainerSlotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateCardContainerSlotPayload::containerId,
            ByteBufCodecs.INT,
            UpdateCardContainerSlotPayload::stateId,
            ByteBufCodecs.INT,
            UpdateCardContainerSlotPayload::slotId,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, Card.STREAM_CODEC),
            UpdateCardContainerSlotPayload::cards,
            UpdateCardContainerSlotPayload::new
    );

    public static void handleClient(UpdateCardContainerSlotPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if(player != null && player.containerMenu instanceof AbstractCardMenu<?, ?> cardMenu && cardMenu.containerId == payload.containerId) {
                cardMenu.setCards(payload.slotId, payload.stateId, payload.cards);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
