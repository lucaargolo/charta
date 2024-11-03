package dev.lucaargolo.charta.utils;


import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeUtils {

    public static VoxelShape rotate(VoxelShape shape, Direction to) {
        return rotate(shape, Direction.NORTH, to);
    }

    public static VoxelShape rotate(VoxelShape shape, Direction from, Direction to) {
        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;

        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.or(buffer[1], Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX));
            });
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }
}

