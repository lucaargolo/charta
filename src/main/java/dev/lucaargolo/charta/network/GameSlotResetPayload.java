package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class GameSlotResetPayload implements CustomPacketPayload  {

    private final BlockPos pos;

    public GameSlotResetPayload(BlockPos pos) {
        this.pos = pos;
    }

    public GameSlotResetPayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static void handleClient(GameSlotResetPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> resetGameSlots(payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void resetGameSlots(GameSlotResetPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(CardTableBlockEntity::resetSlots);
        }
    }

}
