package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.utils.DyeColorHelper;
import dev.lucaargolo.charta.utils.VoxelShapeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GameChairBlock extends BarStoolBlock {

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.box(3, 5, 3, 13, 7, 13),
            Block.box(3, 0, 11, 5, 5, 13),
            Block.box(3, 7, 12, 13, 24, 13),
            Block.box(11, 0, 11, 13, 5, 13),
            Block.box(11, 0, 3, 13, 5, 5),
            Block.box(3, 0, 3, 5, 5, 5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape SOUTH_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.SOUTH);
    private static final VoxelShape EAST_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.EAST);
    private static final VoxelShape WEST_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.WEST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GameChairBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CLOTH, false)
                .setValue(COLOR, DyeColor.WHITE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CLOTH, COLOR);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(tryAndSetCloth(state, level, pos, player)) {
            return InteractionResult.SUCCESS;
        }else if (!state.getValue(CLOTH)) {
            if(!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("charta.message.put_chair_cloth").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }else{
            if(tryAndSit(state, level, pos, player)) {
                return InteractionResult.SUCCESS;
            }else{
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        Direction facing = getValidTable(level, pos);
        if(facing != null) {
            return state.setValue(FACING, facing);
        }else{
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = getValidTable(context.getLevel(), context.getClickedPos());
        return direction == null ? null : this.defaultBlockState().setValue(FACING, direction);
    }

    private @Nullable Direction getValidTable(LevelAccessor level, BlockPos pos) {
        List<Direction> valid = new ArrayList<>();
        for(Direction d : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(pos.relative(d));
            if(state.getBlock() instanceof CardTableBlock block && block.isValidMultiblock(level, pos.relative(d))) {
                int connections = 0;
                connections += state.getValue(CardTableBlock.NORTH) ? 1 : 0;
                connections += state.getValue(CardTableBlock.SOUTH) ? 1 : 0;
                connections += state.getValue(CardTableBlock.EAST) ? 1 : 0;
                connections += state.getValue(CardTableBlock.WEST) ? 1 : 0;
                if(connections == 3) {
                    valid.add(d);
                }
            }
        }
        return valid.size() == 1 ? valid.getFirst() : null;
    }



}
