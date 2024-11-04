package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record UpdateCardContainerSlotPayload(int containerId, int stateId, int slotId, List<Card> cards) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateCardContainerSlotPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("update_card_container_slot"));

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

    public static void handleClient(UpdateCardContainerSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if(player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
                cardMenu.setCards(payload.slotId, payload.stateId, payload.cards);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
