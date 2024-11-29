package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.CardTableScreen;
import dev.lucaargolo.charta.game.CardDeck;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenCardTableScreenPayload(BlockPos pos, CardDeck deck, int[] players) implements CustomPacketPayload {

    public static final Type<OpenCardTableScreenPayload> TYPE = new Type<>(Charta.id("open_card_table_screen"));

    public static final StreamCodec<ByteBuf, OpenCardTableScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenCardTableScreenPayload::pos,
            CardDeck.STREAM_CODEC,
            OpenCardTableScreenPayload::deck,
            new StreamCodec<>() {
                @Override
                public void encode(@NotNull ByteBuf buffer, int @NotNull [] value) {
                    buffer.writeInt(value.length);
                    for (int i : value) {
                        buffer.writeInt(i);
                    }
                }

                @Override
                public int @NotNull [] decode(@NotNull ByteBuf buffer) {
                    int length = buffer.readInt();
                    int[] array = new int[length];
                    for(int i = 0; i < length; i++) {
                        array[i] = buffer.readInt();
                    }
                    return array;
                }
            },
            OpenCardTableScreenPayload::players,
            OpenCardTableScreenPayload::new
    );

    public static void handleClient(OpenCardTableScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            openScreen(payload.pos, payload.deck, payload.players);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(BlockPos pos, CardDeck deck, int[] players) {
        Minecraft.getInstance().setScreen(new CardTableScreen(pos, deck, players));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
