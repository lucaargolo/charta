package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.common.block.entity.CardTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getType()Lnet/minecraft/world/level/block/entity/BlockEntityType;"), method = "method_31716", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void charta$clearTableSlots(BlockPos pos, BlockEntityType<?> entityType, CompoundTag compoundTag, CallbackInfo ci, BlockEntity blockEntity) {
        if (blockEntity instanceof CardTableBlockEntity cardTableBlockEntity && compoundTag != null && blockEntity.getType() == entityType) {
            cardTableBlockEntity.resetSlots();
        }
    }

}
