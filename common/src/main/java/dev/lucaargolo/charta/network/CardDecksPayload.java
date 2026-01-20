package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.Deck;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.concurrent.Executor;

public record CardDecksPayload(LinkedHashMap<ResourceLocation, Deck> cardDecks) implements CustomPacketPayload {

    public static final Type<CardDecksPayload> TYPE = new Type<>(ChartaMod.id("card_decks"));

    public static final StreamCodec<ByteBuf, CardDecksPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, Deck.STREAM_CODEC),
        CardDecksPayload::cardDecks,
        CardDecksPayload::new
    );

    public static void handleClient(CardDecksPayload payload, Executor executor) {
        executor.execute(() -> {
            ChartaMod.CARD_DECKS.setDecks(payload.cardDecks);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
