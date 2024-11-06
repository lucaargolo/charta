package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract Level level();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), method = "interact", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void interactIronLead(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, Leashable leashable, ItemStack itemstack) {
        if (itemstack.is(ModItems.IRON_LEAD) && !leashable.isLeashed() && leashable instanceof LeashableMixed mixed) {
            if (!this.level().isClientSide()) {
                leashable.setLeashedTo(player, true);
                mixed.charta_setIronLeash(true);
            }

            itemstack.shrink(1);
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

}
