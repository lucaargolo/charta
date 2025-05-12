package dev.lucaargolo.charta.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

import java.util.Iterator;

public record BlockBox(BlockPos min, BlockPos max) implements Iterable<BlockPos> {

    public BlockBox(BlockPos min, BlockPos max) {
        this.min = min(min, max);
        this.max = max(min, max);
    }

    private static BlockPos min(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
                Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ())
        );
    }

    private static BlockPos max(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
                Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    public static BlockBox of(BlockPos pos) {
        return new BlockBox(pos, pos);
    }

    public static BlockBox of(BlockPos pos1, BlockPos pos2) {
        return new BlockBox(pos1, pos2);
    }

    public BlockBox include(BlockPos pos) {
        return new BlockBox(min(this.min, pos), max(this.max, pos));
    }

    public boolean isBlock() {
        return this.min.equals(this.max);
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= this.min.getX()
                && pos.getY() >= this.min.getY()
                && pos.getZ() >= this.min.getZ()
                && pos.getX() <= this.max.getX()
                && pos.getY() <= this.max.getY()
                && pos.getZ() <= this.max.getZ();
    }

    public AABB aabb() {
        return encapsulatingFullBlocks(this.min, this.max);
    }

    private static AABB encapsulatingFullBlocks(BlockPos startPos, BlockPos endPos) {
        return new AABB(
                (double)Math.min(startPos.getX(), endPos.getX()),
                (double)Math.min(startPos.getY(), endPos.getY()),
                (double)Math.min(startPos.getZ(), endPos.getZ()),
                (double)(Math.max(startPos.getX(), endPos.getX()) + 1),
                (double)(Math.max(startPos.getY(), endPos.getY()) + 1),
                (double)(Math.max(startPos.getZ(), endPos.getZ()) + 1)
        );
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.betweenClosed(this.min, this.max).iterator();
    }

    public int sizeX() {
        return this.max.getX() - this.min.getX() + 1;
    }

    public int sizeY() {
        return this.max.getY() - this.min.getY() + 1;
    }

    public int sizeZ() {
        return this.max.getZ() - this.min.getZ() + 1;
    }

    public BlockBox extend(Direction direction, int amount) {
        if (amount == 0) {
            return this;
        } else {
            return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE
                    ? of(this.min, max(this.min, this.max.relative(direction, amount)))
                    : of(min(this.min.relative(direction, amount), this.max), this.max);
        }
    }

    public BlockBox move(Direction direction, int amount) {
        return amount == 0 ? this : new BlockBox(this.min.relative(direction, amount), this.max.relative(direction, amount));
    }

    public BlockBox offset(Vec3i vector) {
        return new BlockBox(this.min.offset(vector), this.max.offset(vector));
    }
}
