package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.GameSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class GameSlotPositionPayload implements CustomPacketPayload {

    private final BlockPos pos;
    private final int index;
    private final float x;
    private final float y;
    private final float z;
    private final float angle;

    public GameSlotPositionPayload(BlockPos pos, int index, float x, float y, float z, float angle) {
        this.pos = pos;
        this.index = index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
    }

    public GameSlotPositionPayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.index = buf.readInt();
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
        this.angle = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(angle);
    }

    public static void handleClient(GameSlotPositionPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> updateGameSlot(payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateGameSlot(GameSlotPositionPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                GameSlot slot = cardTable.getSlot(payload.index);
                slot.setX(payload.x);
                slot.setY(payload.y);
                slot.setZ(payload.z);
                slot.setAngle(payload.angle);
            });
        }
    }

}
