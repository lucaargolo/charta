package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.network.GameSlotCompletePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;broadcast(Ljava/util/List;Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.AFTER), method = "broadcastBlockEntity", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setupCardTable(List<ServerPlayer> players, Level level, BlockPos pos, CallbackInfo ci, BlockEntity blockentity) {
        if(blockentity instanceof CardTableBlockEntity cardTable) {
            int count = cardTable.getSlotCount();
            for(int i = 0; i < count; i++) {
                GameSlot slot = cardTable.getSlot(i);
                GameSlotCompletePayload payload = new GameSlotCompletePayload(pos, i, slot);
                players.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
            }
        }

    }


}
