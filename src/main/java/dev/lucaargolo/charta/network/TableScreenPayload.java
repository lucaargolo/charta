package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.TableScreen;
import dev.lucaargolo.charta.game.Deck;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record TableScreenPayload(BlockPos pos, Deck deck, int[] players) implements CustomPacketPayload {

    public static final Type<TableScreenPayload> TYPE = new Type<>(Charta.id("table_screen"));

    public static final StreamCodec<ByteBuf, TableScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TableScreenPayload::pos,
            Deck.STREAM_CODEC,
            TableScreenPayload::deck,
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
            TableScreenPayload::players,
            TableScreenPayload::new
    );

    public static void handleClient(Player player, TableScreenPayload payload) {
        openScreen(payload.pos, payload.deck, payload.players);
    }

    @Environment(EnvType.CLIENT)
    private static void openScreen(BlockPos pos, Deck deck, int[] players) {
        Minecraft.getInstance().setScreen(new TableScreen(pos, deck, players));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
