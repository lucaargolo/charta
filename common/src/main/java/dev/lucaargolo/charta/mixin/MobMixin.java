package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements LeashableMixed {

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void init(CallbackInfo ci) {
        ChartaMod.MOB_IRON_LEASH = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BOOLEAN);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(ChartaMod.MOB_IRON_LEASH, false);
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("charta_isIronLeash", charta_isIronLeash());
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        charta_setIronLeash(tag.getBoolean("charta_isIronLeash"));
    }

    @Override
    public boolean charta_isIronLeash() {
        return entityData.get(ChartaMod.MOB_IRON_LEASH);
    }

    @Override
    public void charta_setIronLeash(boolean value) {
        entityData.set(ChartaMod.MOB_IRON_LEASH, value);
    }



}
