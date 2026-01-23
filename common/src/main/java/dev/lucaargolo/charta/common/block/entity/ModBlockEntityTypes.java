package dev.lucaargolo.charta.common.block.entity;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.ModBlocks;
import dev.lucaargolo.charta.common.registry.ModBlockEntityTypeRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityTypes {

    private static final Supplier<Block[]> CARD_TABLE_BLOCKS = () -> ModBlocks.CARD_TABLE_MAP.values()
            .stream()
            .map(MinecraftEntry::get)
            .toArray(Block[]::new);

    private static final Supplier<Block[]> BAR_SHELF_BLOCKS = () -> ModBlocks.BAR_SHELF_MAP.values()
            .stream()
            .map(MinecraftEntry::get)
            .toArray(Block[]::new);

    public static final ModBlockEntityTypeRegistry REGISTRY = ChartaMod.blockEntityTypeRegistry();

    public static final MinecraftEntry<BlockEntityType<CardTableBlockEntity>> CARD_TABLE = REGISTRY.register("card_table", CardTableBlockEntity::new, CARD_TABLE_BLOCKS);
    public static final MinecraftEntry<BlockEntityType<BarShelfBlockEntity>> BAR_SHELF = REGISTRY.register("bar_shelf", BarShelfBlockEntity::new, BAR_SHELF_BLOCKS);

}
