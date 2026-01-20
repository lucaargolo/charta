package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.utils.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class BeerGlassBlock extends TransparentBlock {

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.box(5, 0, 5, 11, 9, 11),
            Block.box(7.5, 2, 2.5, 8.5, 3, 5),
            Block.box(7.5, 6, 2.5, 8.5, 7, 5),
            Block.box(7.5, 3, 2.5, 8.5, 6, 3.5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape NORTH_SHAPE = VoxelShapeUtils.rotate(EAST_SHAPE, Direction.EAST, Direction.NORTH);
    private static final VoxelShape WEST_SHAPE = VoxelShapeUtils.rotate(EAST_SHAPE, Direction.EAST, Direction.WEST);
    private static final VoxelShape SOUTH_SHAPE = VoxelShapeUtils.rotate(EAST_SHAPE, Direction.EAST, Direction.SOUTH);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 300, 2), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
            .build();

    public BeerGlassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(!state.is(ModBlocks.EMPTY_BEER_GLASS)) {
            if(!level.isClientSide()) {
                player.eat(level, this.asItem().getDefaultInstance(), FOOD);
                level.setBlockAndUpdate(pos, ModBlocks.EMPTY_BEER_GLASS.get().defaultBlockState().setValue(FACING, state.getValue(FACING)));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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
        Direction direction = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction);
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

}
