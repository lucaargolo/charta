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
        https://github.com/starfish-studios/AnotherFurniture/blob/1.21.1/common/src/main/java/com/starfish_studios/another_furniture/entity/SeatEntity.java
    At:
        02/11/2024
 */

package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.block.GameChairBlock;
import dev.lucaargolo.charta.block.SeatBlock;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SeatEntity extends Entity {

    public SeatEntity(EntityType<SeatEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos) {
        this(ModEntityTypes.SEAT, level);
        this.setPos(pos.getX() + 0.5, pos.getY() + 0.001, pos.getZ() + 0.5);
    }
    @Override
    public void tick() {
        if (this.level().isClientSide) return;

        BlockState state = this.level().getBlockState(this.blockPosition());
        if(state.getBlock() instanceof GameChairBlock) {
            for(Entity entity : this.getPassengers()) {
                if(!(entity instanceof Player)) {
                    Direction facing = state.getValue(GameChairBlock.FACING);
                    entity.lookAt(EntityAnchorArgument.Anchor.EYES, entity.getEyePosition().add(Vec3.atLowerCornerOf(facing.getNormal()).multiply(2f, 2f, 2f)));
                }
            }
        }

        boolean canSit;
        if (state.getBlock() instanceof SeatBlock seatBlock) canSit = seatBlock.isSittable(state);
        else canSit = false;
        if (isVehicle() && canSit) return;

        this.discard();
        this.level().updateNeighbourForOutputSignal(this.blockPosition(), this.level().getBlockState(this.blockPosition()).getBlock());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {}

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return this.position().add(new Vec3(0.0, getPassengersRidingOffset(), 0.0));
    }

    public double getPassengersRidingOffset() {
        List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) return 0.0;
        double seatHeight = 0.0;
        BlockState state = this.level().getBlockState(this.blockPosition());
        if (state.getBlock() instanceof SeatBlock seatBlock) seatHeight = seatBlock.seatHeight(state);

        return seatHeight + getEntitySeatOffset(passengers.getFirst());
    }

    public static double getEntitySeatOffset(Entity entity) {
        if (entity instanceof Slime) return 1 / 4f;
        if (entity instanceof Parrot) return 1 / 16f;
        if (entity instanceof Skeleton) return 1 / 8f;
        if (entity instanceof Creeper) return 1 / 4f;
        if (entity instanceof Cat) return 1 / 8f;
        if (entity instanceof Wolf) return 1 / 16f;
        return 0;
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return true;
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(@NotNull LivingEntity entity) {
        BlockPos pos = this.blockPosition();
        Vec3 safeVec;
        BlockState state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof SeatBlock seatBlock) {
            safeVec = DismountHelper.findSafeDismountLocation(entity.getType(), this.level(), seatBlock.primaryDismountLocation(this.level(), state, pos), false);
            if (safeVec != null) return safeVec.add(0, 0.25, 0);
        }

        Direction original = this.getDirection();
        Direction[] offsets = {original, original.getClockWise(), original.getCounterClockWise(), original.getOpposite()};
        for(Direction dir : offsets) {
            safeVec = DismountHelper.findSafeDismountLocation(entity.getType(), this.level(), pos.relative(dir), false);
            if (safeVec != null) return safeVec.add(0, 0.25, 0);
        }
        return super.getDismountLocationForPassenger(entity);
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        BlockPos pos = this.blockPosition();
        BlockState state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof SeatBlock seatBlock) passenger.setYRot(seatBlock.setRiderRotation(state, passenger));
        super.addPassenger(passenger);
    }

    @Override
    protected void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);
        if (entity instanceof TamableAnimal ta) ta.setInSittingPose(false);
    }
}