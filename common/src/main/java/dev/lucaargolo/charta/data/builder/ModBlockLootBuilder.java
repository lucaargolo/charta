package dev.lucaargolo.charta.data.builder;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Function;

public interface ModBlockLootBuilder {

    void otherWhenSilkTouch(Block block, Block other);

    void dropWhenSilkTouch(Block block);

    void dropOther(Block block, ItemLike item);

    void dropSelf(Block block);

    void add(Block block, Function<Block, LootTable.Builder> factory);

    void add(Block block, LootTable.Builder builder);

}
