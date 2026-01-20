package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

public record UpdateCardContainerCarriedPayload(int containerId, int stateId, List<Card> cards) implements CustomPacketPayload {

    public static final Type<UpdateCardContainerCarriedPayload> TYPE = new Type<>(ChartaMod.id("update_card_container_carried"));

    public static final StreamCodec<ByteBuf, UpdateCardContainerCarriedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateCardContainerCarriedPayload::containerId,
            ByteBufCodecs.INT,
            UpdateCardContainerCarriedPayload::stateId,
            ByteBufCodecs.collection(i -> new LinkedList<>(), Card.STREAM_CODEC),
            UpdateCardContainerCarriedPayload::cards,
            UpdateCardContainerCarriedPayload::new
    );

    public static void handleClient(UpdateCardContainerCarriedPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if(player != null && player.containerMenu instanceof AbstractCardMenu<?> cardMenu && cardMenu.containerId == payload.containerId) {
                cardMenu.setCarriedCards(payload.stateId, new GameSlot(payload.cards));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
