/*
    MIT License

    Copyright (c) 2022 Starfish Studios, Inc.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

    Code extracted from:
        https://github.com/starfish-studios/AnotherFurniture/blob/1.21.1/common/src/main/java/com/starfish_studios/another_furniture/block/SeatBlock.java
    At:
        02/11/2024
 */

package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class SeatBlock extends Block {

    public SeatBlock(Properties properties) {
        super(properties);
    }

    public float seatHeight(BlockState state) {
        return 0.35F;
    }

    public boolean isSittable(BlockState state) {
        return true;
    }

    public BlockPos primaryDismountLocation(Level level, BlockState state, BlockPos pos) {
        return pos;
    }

    public float setRiderRotation(BlockState state, Entity entity) {
        return entity.getYRot();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(tryAndSit(state, level, pos, player)) {
            return InteractionResult.SUCCESS;
        }else{
            return InteractionResult.PASS;
        }
    }

    public boolean tryAndSit(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        if (player instanceof FakePlayer) return false;
        if (!level.mayInteract(player, pos)) return false;
        if (!isSittable(state) || player.isPassenger() || player.isCrouching()) return false;

        if (isSeatBlocked(level, pos)) return false;
        if (isSeatOccupied(level, pos)) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
            return ejectSeatedExceptPlayer(level, seats.getFirst());
        }


        if (level.isClientSide)  return true;
        sitDown(level, pos, getLeashed(player).orElse(player));
        return true;
    }

    public static boolean isSeatBlocked(Level level, BlockPos pos) {
        return !(level.getBlockState(pos.above()).getCollisionShape(level, pos).isEmpty());
    }

    public static boolean isSeatOccupied(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(SeatEntity.class, new AABB(pos)).isEmpty();
    }

    public static Optional<Entity> getLeashed(Player player) {
        List<Entity> entities = player.level().getEntities((Entity) null, player.getBoundingBox().inflate(10), e -> true);
        for (Entity e : entities)
            if (e instanceof Mob mob && mob.getLeashHolder() == player && canBePickedUp(e)) return Optional.of(mob);
        return Optional.empty();
    }

    public static boolean ejectSeatedExceptPlayer(Level level, SeatEntity seatEntity) {
        List<Entity> passengers = seatEntity.getPassengers();
        if (!passengers.isEmpty() && passengers.getFirst() instanceof Player) return false;
        if (!level.isClientSide) seatEntity.ejectPassengers();
        return true;
    }

    public static boolean canBePickedUp(Entity passenger) {
        if (passenger instanceof Player) return false;
        if (passenger instanceof TamableAnimal ta && !ta.isTame()) return false;
        return passenger instanceof LivingEntity;
    }

    public static void sitDown(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        SeatEntity seat = new SeatEntity(level, pos);
        level.addFreshEntity(seat);
        entity.startRiding(seat);

        level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());

        if (entity instanceof TamableAnimal ta) ta.setInSittingPose(true);
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return isSeatOccupied(level, pos) ? 15 : 0;
    }
}