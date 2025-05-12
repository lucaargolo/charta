package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class CardTableSelectGamePayload implements CustomPacketPayload {

    private final BlockPos pos;
    private final ResourceLocation gameId;
    private final byte[] options;

    public CardTableSelectGamePayload(BlockPos pos, ResourceLocation gameId, byte[] options) {
        this.pos = pos;
        this.gameId = gameId;
        this.options = options;
    }

    public CardTableSelectGamePayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.gameId = buf.readResourceLocation();
        this.options = buf.readByteArray();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(gameId);
        buf.writeByteArray(options);
    }

    public static void handleServer(CardTableSelectGamePayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> context.getSender().level().getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(table -> {
            if(table.getGame() == null || table.getGame().isGameOver()) {
                Component result = table.startGame(payload.gameId, payload.options);
                context.getSender().displayClientMessage(result, true);
            }
        }));
    }


}
