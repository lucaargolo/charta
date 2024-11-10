package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.AutoPlayer;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.CardPlayerHead;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityMixed {

    @Unique
    private final CardPlayer charta_cardPlayer = new AutoPlayer(random.nextFloat()) {

        @Override
        public void handUpdated() {
            entityData.set(Charta.ENTITY_HAND, getHand());
        }

        @Override
        public Component getName() {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            return living.getDisplayName();
        }

        @Override
        public int getId() {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            return living.getId();
        }

        @Override
        public CardPlayerHead getHead() {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            return CardPlayerHead.get(living);
        }

        @Override
        public DyeColor getColor() {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            if(living.getVehicle() instanceof SeatEntity seatEntity) {
                BlockState state = seatEntity.getBlockStateOn();
                if(state.getBlock() instanceof GameChairBlock) {
                    return state.getValue(GameChairBlock.COLOR);
                }
            }
            return DyeColor.WHITE;
        }
    };

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void init(CallbackInfo ci) {
        Charta.ENTITY_HAND = SynchedEntityData.defineId(LivingEntity.class, ModEntityDataSerializers.CARD_LIST);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(Charta.ENTITY_HAND, new ArrayList<>());
    }

    @Override
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

}
