package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.AutoPlayer;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityMixed {

    @Unique
    private final CardPlayer charta_cardPlayer = new AutoPlayer(random.nextFloat()) {

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

        @Override
        public LivingEntity getEntity() {
            return (LivingEntity) (Object) LivingEntityMixin.this;
        }
    };

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

    @Inject(at = @At("HEAD"), method = "stopRiding")
    public void dismountVehicle(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
        if(!living.isRemoved() && living.getVehicle() instanceof SeatEntity seatEntity) {
            Level level = seatEntity.level();
            BlockPos pos = seatEntity.getOnPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof GameChairBlock) {
                Direction facing = state.getValue(GameChairBlock.FACING);
                BlockState tableState = level.getBlockState(pos.relative(facing));
                if(tableState.getBlock() instanceof CardTableBlock cardTableBlock) {
                    BlockPos center = cardTableBlock.getCenterAndOffset(level, pos.relative(facing)).getFirst();
                    level.getBlockEntity(center, ModBlockEntityTypes.CARD_TABLE).ifPresent(blockEntity -> {
                        blockEntity.playersDirty = true;
                    });
                }
            }
        }
    }

}
