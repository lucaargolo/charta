package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardHolderMixed;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements CardHolderMixed {

    @Unique
    private final List<Card> charta_hand = new ArrayList<>();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(Charta.PLAYER_HAND, new ArrayList<>());
    }

    @Override
    public List<Card> charta_getHand() {
        return charta_hand;
    }

    @Override
    public void charta_handUpdated() {
        this.entityData.set(Charta.PLAYER_HAND, charta_hand);
    }

}
