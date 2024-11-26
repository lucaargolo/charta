package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;

public record CardPlayPayload(Component playerName, int playerCards, Component play) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CardPlayPayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("card_play"));

    public static StreamCodec<ByteBuf, CardPlayPayload> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        CardPlayPayload::playerName,
        ByteBufCodecs.INT,
        CardPlayPayload::playerCards,
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC,
        CardPlayPayload::play,
        CardPlayPayload::new
    );

    public static void handleClient(CardPlayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            addToHistory(payload.playerName, payload.playerCards, payload.play);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void addToHistory(Component playerName, int playerCards, Component play) {
        ChartaClient.LOCAL_HISTORY.add(ImmutableTriple.of(playerName, playerCards, play));
        //TODO: Remove this
        if(playerName.getString().equals("Table")) {
            System.out.println(play.getString());
        }else{
            System.out.println(playerName.getString() + " | "+playerCards+" cards | "+play.getString());
        }
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
