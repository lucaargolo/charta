package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.AutoPlayer;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
        public BlockPos getPosition() {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            return living.blockPosition();
        }
    };

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

}
