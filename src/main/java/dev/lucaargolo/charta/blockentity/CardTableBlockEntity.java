package dev.lucaargolo.charta.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CardTableBlockEntity extends BlockEntity {

    public CardTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CARD_TABLE.get(), pos, blockState);
    }

}
