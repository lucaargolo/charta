package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(LeadItem.class)
public class LeadItemMixin {

    @Unique
    private static Level charta$capturedLevel;
    @Unique
    private static BlockPos charta$capturedPos;

    @Inject(at = @At("HEAD"), method = "bindPlayerMobs")
    private static void captureVariables(Player pPlayer, Level pLevel, BlockPos pPos, CallbackInfoReturnable<InteractionResult> cir) {
        charta$capturedLevel = pLevel;
        charta$capturedPos = pPos;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), method = "bindPlayerMobs")
    private static Iterator<Mob> ironBindPlayerMobs(List<Mob> instance) {
        LeashFenceKnotEntity leashfenceknotentity = null;
        Iterator<Mob> iterator = instance.iterator();
        while (iterator.hasNext()) {
            Mob leashable = iterator.next();
            if(leashable instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
                if (leashfenceknotentity == null) {
                    leashfenceknotentity = IronLeashFenceKnotEntity.getOrCreateIronKnot(charta$capturedLevel, charta$capturedPos);
                    leashfenceknotentity.playPlacementSound();
                }

                leashable.setLeashedTo(leashfenceknotentity, true);

                iterator.remove();
            }
        }

        return iterator;
    }

    @Inject(at = @At("TAIL"), method = "bindPlayerMobs")
    private static void releaseVariables(Player pPlayer, Level pLevel, BlockPos pPos, CallbackInfoReturnable<InteractionResult> cir) {
        charta$capturedLevel = null;
        charta$capturedPos = null;
    }


}
