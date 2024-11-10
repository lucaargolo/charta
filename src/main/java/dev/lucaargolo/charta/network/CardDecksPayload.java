package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public record CardDecksPayload(HashMap<ResourceLocation, CardDeck> cardDecks) implements CustomPacketPayload {

    public static final Type<CardDecksPayload> TYPE = new Type<>(Charta.id("card_decks"));

    public static final StreamCodec<ByteBuf, CardDecksPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, CardDeck.STREAM_CODEC),
        CardDecksPayload::cardDecks,
        CardDecksPayload::new
    );

    public static void handleClient(CardDecksPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Charta.CARD_DECKS.setDecks(payload.cardDecks);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
