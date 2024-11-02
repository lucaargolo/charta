package dev.lucaargolo.charta.block;

import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardTableBlock extends Block {

    private static final List<Vector2i> VALID_DIMENSIONS = List.of(new Vector2i(3, 3), new Vector2i(4, 3), new Vector2i(5, 3));
    private static final int MAX_SIZE;

    public static final BooleanProperty VALID = BooleanProperty.create("valid");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CLOTH = BooleanProperty.create("cloth");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    static {
        Vector2i last = VALID_DIMENSIONS.getLast();
        MAX_SIZE = last.x * last.y;
    }

    public CardTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(VALID, false)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(CLOTH, false)
            .setValue(COLOR, DyeColor.WHITE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VALID, NORTH, EAST, SOUTH, WEST, CLOTH, COLOR);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(player instanceof ServerPlayer serverPlayer) {
            System.out.println(getCenterPos(level, pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        boolean valid = isValidMultiblock(level, pos);
        return switch (direction) {
            case Direction.NORTH -> state.setValue(VALID, valid).setValue(NORTH, neighborState.is(this));
            case Direction.EAST -> state.setValue(VALID, valid).setValue(EAST, neighborState.is(this));
            case Direction.SOUTH -> state.setValue(VALID, valid).setValue(SOUTH, neighborState.is(this));
            case Direction.WEST -> state.setValue(VALID, valid).setValue(WEST, neighborState.is(this));
            default -> super.updateShape(state, direction, neighborState, level, pos, neighborPos).setValue(VALID, valid);
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean valid = isValidMultiblock(context.getLevel(), context.getClickedPos());
        return this.defaultBlockState().setValue(VALID, valid)
                .setValue(NORTH, context.getLevel().getBlockState(context.getClickedPos().north()).is(this))
                .setValue(EAST, context.getLevel().getBlockState(context.getClickedPos().east()).is(this))
                .setValue(SOUTH, context.getLevel().getBlockState(context.getClickedPos().south()).is(this))
                .setValue(WEST, context.getLevel().getBlockState(context.getClickedPos().west()).is(this));
    }

    public BlockPos getCenterPos(LevelAccessor level, BlockPos pos) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> multiblock = new HashSet<>();

        floodFill(level, pos, visited, multiblock);
        BlockBox box = getBoundingBox(multiblock);
        BlockPos min = box.min();
        BlockPos max = box.max();

        return new BlockPos(min.getX() + Mth.floor((max.getX() - min.getX())/2.0), min.getY() + Mth.floor((max.getY() - min.getY())/2.0), min.getZ() + Mth.floor((max.getZ() - min.getZ())/2.0));
    }

    public boolean isValidMultiblock(LevelAccessor level, BlockPos pos) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> multiblock = new HashSet<>();

        floodFill(level, pos, visited, multiblock);

        int width = getWidth(multiblock);
        int height = getHeight(multiblock);

        return isValidDimensions(width, height) && isBoundingBoxFilled(level, getBoundingBox(multiblock));
    }

    private void floodFill(LevelAccessor level, BlockPos pos, Set<BlockPos> visited, Set<BlockPos> multiblock) {
        if (multiblock.size() > MAX_SIZE) return;
        if (!visited.add(pos)) return;
        if (level.getBlockState(pos).getBlock() != this) return;
        multiblock.add(pos);

        floodFill(level, pos.north(), visited, multiblock);
        floodFill(level, pos.south(), visited, multiblock);
        floodFill(level, pos.east(), visited, multiblock);
        floodFill(level, pos.west(), visited, multiblock);
    }

    private BlockBox getBoundingBox(Set<BlockPos> multiblock) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int y = 0;

        for (BlockPos pos : multiblock) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            y = pos.getY();
        }

        return BlockBox.of(new BlockPos(minX, y, minZ), new BlockPos(maxX, y, maxZ));
    }

    private boolean isBoundingBoxFilled(LevelAccessor level, BlockBox boundingBox) {
        for(BlockPos pos : boundingBox) {
            if (level.getBlockState(pos).getBlock() != this) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidDimensions(int width, int height) {
        return VALID_DIMENSIONS.contains(new Vector2i(width, height)) || VALID_DIMENSIONS.contains(new Vector2i(height, width));
    }

    private int getWidth(Set<BlockPos> multiblock) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (BlockPos pos : multiblock) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
        }
        return maxX - minX + 1;
    }

    private int getHeight(Set<BlockPos> multiblock) {
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : multiblock) {
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return maxZ - minZ + 1;
    }

}
