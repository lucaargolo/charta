package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.block.entity.CardTableBlockEntity;
import dev.lucaargolo.charta.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.entity.SeatEntity;
import dev.lucaargolo.charta.game.Game;
import dev.lucaargolo.charta.network.GameLeavePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("RETURN"), method = "wantsToStopRiding", cancellable = true)
    public void confirmStopGame(CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue() && this.getVehicle() instanceof SeatEntity seatEntity) {
            Level level = seatEntity.level();
            BlockPos pos = seatEntity.getOnPos();
            BlockState state = level.getBlockState(pos);
            if(state.getBlock() instanceof GameChairBlock) {
                Direction facing = state.getValue(GameChairBlock.FACING);
                BlockState tableState = level.getBlockState(pos.relative(facing));
                if(tableState.getBlock() instanceof CardTableBlock cardTableBlock) {
                    BlockPos center = cardTableBlock.getCenterAndOffset(level, pos.relative(facing)).getFirst();
                    Optional<CardTableBlockEntity> optional = level.getBlockEntity(center, ModBlockEntityTypes.CARD_TABLE.get());
                    if(optional.isPresent()) {
                        CardTableBlockEntity blockEntity = optional.get();
                        Game<?, ?> currentGame = blockEntity.getGame();
                        if(currentGame != null && !currentGame.isGameOver()) {
                            if((Object) this instanceof ServerPlayer serverPlayer) {
                                ChartaMod.getPacketManager().sendToPlayer(serverPlayer, new GameLeavePayload());
                            }
                            cir.setReturnValue(false);
                        }
                    }
                }
            }
        }
    }

}
