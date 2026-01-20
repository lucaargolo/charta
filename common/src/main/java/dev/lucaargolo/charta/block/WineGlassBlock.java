package dev.lucaargolo.charta.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class WineGlassBlock extends Block {

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(6.5, 0, 6.5, 9.5, 0.5, 9.5),
            Block.box(6.5, 5, 6.5, 9.5, 11, 9.5),
            Block.box(7.5, 0.5, 7.5, 8.5, 5, 8.5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1), 1.0F)
            .effect(new MobEffectInstance(MobEffects.HUNGER, 300, 2), 1.0F)
            .effect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
            .build();


    public WineGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if(!state.is(ModBlocks.EMPTY_WINE_GLASS.get())) {
            if(!level.isClientSide()) {
                player.eat(level, this.asItem().getDefaultInstance(), FOOD);
                level.setBlockAndUpdate(pos, ModBlocks.EMPTY_WINE_GLASS.get().defaultBlockState());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}
