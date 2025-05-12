package dev.lucaargolo.charta.block;

import dev.lucaargolo.charta.utils.DyeColorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class BarStoolBlock extends SeatBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(7, 0, 7, 9, 6, 9),
            Block.box(4, 6, 4, 12, 8, 12),
            Block.box(4, 6, 2, 12, 8, 4),
            Block.box(4, 6, 12, 12, 8, 14),
            Block.box(12, 6, 4, 14, 8, 12),
            Block.box(2, 6, 4, 4, 8, 12)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public static final BooleanProperty CLOTH = BooleanProperty.create("cloth");
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);


    public BarStoolBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CLOTH, false)
            .setValue(COLOR, DyeColor.WHITE)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CLOTH, COLOR);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if(!state.is(newState.getBlock()) && state.getValue(CLOTH)) {
            Vec3 center = pos.getCenter();
            ItemStack carpetStack = DyeColorHelper.getCarpet(state.getValue(COLOR)).asItem().getDefaultInstance();
            Containers.dropItemStack(level, center.x, center.y, center.z, carpetStack);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if(tryAndSetCloth(state, level, pos, player)) {
            return InteractionResult.SUCCESS;
        }else if(tryAndSit(state, level, pos, player)) {
            return InteractionResult.SUCCESS;
        }else{
            return InteractionResult.PASS;
        }
    }

    public boolean tryAndSetCloth(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoolCarpetBlock carpetBlock) {
            if (!level.isClientSide()) {
                if(!player.isCreative()) {
                    stack.shrink(1);
                }
                if (!state.getValue(CLOTH)) {
                    DyeColor color = carpetBlock.getColor();
                    level.setBlockAndUpdate(pos, state.setValue(CLOTH, true).setValue(COLOR, color));
                }
            }
            return true;
        }else if(player.isShiftKeyDown()){
            if (!level.isClientSide()) {
                if (state.getValue(CLOTH)) {
                    if(!isSeatOccupied(level, pos)) {
                        DyeColor color = state.getValue(COLOR);
                        level.setBlockAndUpdate(pos, state.setValue(CLOTH, false));
                        Vec3 c = pos.getCenter();
                        Containers.dropItemStack(level, c.x, c.y, c.z, DyeColorHelper.getCarpet(color).asItem().getDefaultInstance());
                    }else{
                        player.displayClientMessage(Component.translatable("message.charta.cant_remove_while_seat_occupied").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}
