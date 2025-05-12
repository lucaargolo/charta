package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;

public class CardDecksPayload implements CustomPacketPayload {

    private final LinkedHashMap<ResourceLocation, CardDeck> cardDecks;

    public CardDecksPayload(LinkedHashMap<ResourceLocation, CardDeck> cardDecks) {
        this.cardDecks = cardDecks;
    }

    public CardDecksPayload(FriendlyByteBuf buf) {
        this.cardDecks = new LinkedHashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.cardDecks.put(buf.readResourceLocation(), CardDeck.fromBuf(buf));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(cardDecks.size());
        cardDecks.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            value.toBuf(buf);
        });
    }

    public static void handleClient(CardDecksPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Charta.CARD_DECKS.setDecks(payload.cardDecks);
        });
        context.setPacketHandled(true);
    }

}
