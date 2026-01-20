package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import dev.lucaargolo.charta.utils.LeashableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

    @Inject(at = @At("HEAD"), method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", cancellable = true)
    private static <E extends Entity & Leashable> void doNotMixLeashTypes(E entity, Entity leashHolder, boolean broadcastPacket, CallbackInfo ci) {
        if(entity instanceof LeashableMixed mixed && mixed.charta_isIronLeash() && leashHolder instanceof LeashFenceKnotEntity && !(leashHolder instanceof IronLeashFenceKnotEntity)) {
            ci.cancel();
        }else if((!(entity instanceof LeashableMixed mixed) || !mixed.charta_isIronLeash()) && leashHolder instanceof IronLeashFenceKnotEntity) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "restoreLeashFromSave")
    private static <E extends Entity & Leashable> void captureRestoreEntity(E p_entity, Leashable.LeashData leashData, CallbackInfo ci) {
        LeashableHelper.capturedRestoreEntity = p_entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", ordinal = 1), method = "restoreLeashFromSave", index = 1)
    private static Entity restoreIronLeashFromSave(Entity entity) {
        if(entity instanceof LeashFenceKnotEntity leashEntity && !(entity instanceof IronLeashFenceKnotEntity) && LeashableHelper.capturedRestoreEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            Level level = leashEntity.level();
            BlockPos pos = leashEntity.getPos();
            leashEntity.kill();
            return IronLeashFenceKnotEntity.getOrCreateIronKnot(level, pos);
        }
        return entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "restoreLeashFromSave")
    private static <E extends Entity & Leashable> ItemLike restoreIronLeashFromSave(ItemLike item) {
        if(LeashableHelper.capturedRestoreEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            mixed.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }

    @Inject(at = @At("TAIL"), method = "restoreLeashFromSave")
    private static <E extends Entity & Leashable> void removeCapturedEntity(E p_entity, Leashable.LeashData leashData, CallbackInfo ci) {
        LeashableHelper.capturedRestoreEntity = null;
    }

    @Inject(at = @At("HEAD"), method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V")
    private static <E extends Entity & Leashable> void captureDropEntity(E entity, boolean broadcastPacket, boolean dropItem, CallbackInfo ci) {
        LeashableHelper.capturedDropEntity = entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V")
    private static <E extends Entity & Leashable> ItemLike dropIronLeash(ItemLike item) {
        if(LeashableHelper.capturedDropEntity instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            mixed.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }

    @Inject(at = @At("TAIL"), method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V")
    private static <E extends Entity & Leashable> void removeCapturedEntity(E entity, boolean broadcastPacket, boolean dropItem, CallbackInfo ci) {
        LeashableHelper.capturedDropEntity = null;
    }

}
