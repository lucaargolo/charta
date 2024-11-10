package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(LeadItem.class)
public class LeadItemMixin {

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), method = "bindPlayerMobs", locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void ironBindPlayerMobs(Player player, Level level, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir, LeashFenceKnotEntity leashfenceknotentity, List<Leashable> list) {
        Iterator<Leashable> iterator = list.iterator();
        while (iterator.hasNext()) {
            Leashable leashable = iterator.next();
            if(leashable instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
                if (leashfenceknotentity == null) {
                    leashfenceknotentity = IronLeashFenceKnotEntity.getOrCreateIronKnot(level, pos);
                    leashfenceknotentity.playPlacementSound();
                }

                leashable.setLeashedTo(leashfenceknotentity, true);

                iterator.remove();
            }
        }

    }

    @Inject(at= @At("RETURN"), method = "leashableInArea", cancellable = true)
    private static void mutableList(Level level, BlockPos pos, Predicate<Leashable> predicate, CallbackInfoReturnable<List<Leashable>> cir) {
        cir.setReturnValue(new ArrayList<>(cir.getReturnValue()));
    }

}
