package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements LeashableMixed {

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void init(CallbackInfo ci) {
        Charta.MOB_IRON_LEASH = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BOOLEAN);
    }

    @Shadow public abstract void setLeashedTo(Entity pLeashHolder, boolean pBroadcastPacket);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0), method = "checkAndHandleImportantInteractions", cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void interactIronLead(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, ItemStack itemstack) {
        if (itemstack.is(ModItems.IRON_LEAD.get())) {
            if (!this.level().isClientSide()) {
                this.setLeashedTo(player, true);
                charta_setIronLeash(true);
            }

            itemstack.shrink(1);
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(CallbackInfo ci) {
        entityData.define(Charta.MOB_IRON_LEASH, false);
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("charta_isIronLeash", charta_isIronLeash());
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        charta_setIronLeash(tag.getBoolean("charta_isIronLeash"));
    }

    @Inject(at = @At("HEAD"), method = "setLeashedTo", cancellable = true)
    private void doNotMixLeashTypes(Entity leashHolder, boolean pBroadcastPacket, CallbackInfo ci) {
        if(this.charta_isIronLeash() && leashHolder instanceof LeashFenceKnotEntity && !(leashHolder instanceof IronLeashFenceKnotEntity)) {
            ci.cancel();
        }else if(!this.charta_isIronLeash() && leashHolder instanceof IronLeashFenceKnotEntity) {
            ci.cancel();
        }
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V", ordinal = 1), method = "restoreLeashFromSave", index = 0)
    private Entity restoreIronLeashFromSave(Entity entity) {
        if(entity instanceof LeashFenceKnotEntity leashEntity && !(entity instanceof IronLeashFenceKnotEntity) && this.charta_isIronLeash()) {
            Level level = leashEntity.level();
            BlockPos pos = leashEntity.getPos();
            leashEntity.kill();
            return IronLeashFenceKnotEntity.getOrCreateIronKnot(level, pos);
        }
        return entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "restoreLeashFromSave")
    private ItemLike restoreIronLeashFromSave(ItemLike item) {
        if(this.charta_isIronLeash()) {
            this.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"), method = "dropLeash")
    private ItemLike dropIronLeash(ItemLike item) {
        if(this.charta_isIronLeash()) {
            this.charta_setIronLeash(false);
            return ModItems.IRON_LEAD.get();
        }
        return item;
    }

    @Override
    public boolean charta_isIronLeash() {
        return entityData.get(Charta.MOB_IRON_LEASH);
    }

    @Override
    public void charta_setIronLeash(boolean value) {
        entityData.set(Charta.MOB_IRON_LEASH, value);
    }



}
