package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.gui.screens.TableScreen;
import dev.lucaargolo.charta.game.Deck;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class TableScreenPayload implements CustomPacketPayload {

    private final BlockPos pos;
    private final Deck deck;
    private final int[] players;

    public TableScreenPayload(BlockPos pos, Deck deck, int[] players) {
        this.pos = pos;
        this.deck = deck;
        this.players = players;
    }

    public TableScreenPayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.deck = Deck.fromBuf(buf);
        int size = buf.readInt();
        this.players = new int[size];
        for (int i = 0; i < players.length; i++) {
            this.players[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        this.deck.toBuf(buf);
        buf.writeInt(players.length);
        for (int player : players) {
            buf.writeInt(player);
        }
    }

    public static void handleClient(TableScreenPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            openScreen(payload.pos, payload.deck, payload.players);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(BlockPos pos, Deck deck, int[] players) {
        Minecraft.getInstance().setScreen(new TableScreen(pos, deck, players));
    }


}
