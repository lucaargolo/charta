package dev.lucaargolo.charta.common.network;

import dev.lucaargolo.charta.client.render.screen.TableScreen;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.card.Deck;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public record TableScreenPayload(BlockPos pos, Deck deck, int[] players) implements CustomPacketPayload {

    public static final Type<TableScreenPayload> TYPE = new Type<>(ChartaMod.id("table_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TableScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TableScreenPayload::pos,
            Deck.STREAM_CODEC,
            TableScreenPayload::deck,
            new StreamCodec<>() {
                @Override
                public void encode(@NotNull RegistryFriendlyByteBuf buffer, int @NotNull [] value) {
                    buffer.writeInt(value.length);
                    for (int i : value) {
                        buffer.writeInt(i);
                    }
                }

                @Override
                public int @NotNull [] decode(@NotNull RegistryFriendlyByteBuf buffer) {
                    int length = buffer.readInt();
                    int[] array = new int[length];
                    for(int i = 0; i < length; i++) {
                        array[i] = buffer.readInt();
                    }
                    return array;
                }
            },
            TableScreenPayload::players,
            TableScreenPayload::new
    );

    public static void handleClient(TableScreenPayload payload, Executor executor) {
        executor.execute(() -> {
            Minecraft.getInstance().setScreen(new TableScreen(payload.pos, payload.deck, payload.players));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
