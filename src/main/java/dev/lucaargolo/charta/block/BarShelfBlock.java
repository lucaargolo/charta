package dev.lucaargolo.charta.block;

import com.mojang.serialization.MapCodec;
import dev.lucaargolo.charta.blockentity.BarShelfBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.utils.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class BarShelfBlock extends BaseEntityBlock {

    public static final MapCodec<BarShelfBlock> CODEC = simpleCodec(BarShelfBlock::new);

    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.SOUTH);
    private static final VoxelShape EAST_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.EAST);
    private static final VoxelShape WEST_SHAPE = VoxelShapeUtils.rotate(NORTH_SHAPE, Direction.WEST);

    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BarShelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UP, false)
        );
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UP);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BarShelfBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock())) {
            level.getBlockEntity(pos, ModBlockEntityTypes.BAR_SHELF.get()).ifPresent(entity -> {
                Containers.dropContents(level, pos, entity);
            });
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        if(direction == state.getValue(FACING)) {
            if(!level.isClientSide()) {
                Vec3 hit = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
                boolean axis = direction.getAxis() == Direction.Axis.X;
                boolean plus = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                Vector2d coords = new Vector2d(axis ? plus ? 1 - hit.z : hit.z : plus ? hit.x : 1 - hit.x, hit.y);
                int slot = coords.x < 0.5 ? coords.y > 0.5 ? 0 : 2 : coords.y > 0.5 ? 1 : 3;
                level.getBlockEntity(pos, ModBlockEntityTypes.BAR_SHELF.get()).ifPresent(entity -> {
                    ItemStack stored = entity.getItem(slot);
                    if(!stored.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(entity.removeItem(slot, 1));
                    }else if(!stack.isEmpty()) {
                        entity.setItem(slot, stack.copyWithCount(1));
                        stack.shrink(1);
                    }
                });
            }


            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos neighborPos = context.getClickedPos().above();
        BlockState neighborState = level.getBlockState(neighborPos);
        return this.defaultBlockState()
                .setValue(FACING, direction)
                .setValue(UP, neighborState.getBlock() instanceof BarShelfBlock || neighborState.isCollisionShapeFullBlock(level, neighborPos));
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if(direction == Direction.UP) {
            return state.setValue(UP, neighborState.getBlock() instanceof BarShelfBlock || neighborState.isCollisionShapeFullBlock(level, neighborPos));
        }else{
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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
}
