package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class FenceBlockMixin {

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void useIronLeashOn(BlockState pState, Level level, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (level.isClientSide && stack.is(ModItems.IRON_LEAD.get())) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

}
