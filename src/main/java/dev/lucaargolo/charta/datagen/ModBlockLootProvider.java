package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {

    protected ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        ModBlocks.CARD_TABLE_MAP.values().forEach(holder -> {
            this.dropSelf(holder.get());
        });
        ModBlocks.GAME_CHAIR_MAP.values().forEach(holder -> {
            this.dropSelf(holder.get());
        });
        ModBlocks.BAR_STOOL_MAP.values().forEach(holder -> {
            this.dropSelf(holder.get());
        });
        this.dropSelf(ModBlocks.EMPTY_BEER_GLASS.get());
        this.dropSelf(ModBlocks.WHEAT_BEER_GLASS.get());
        this.dropSelf(ModBlocks.SORGHUM_BEER_GLASS.get());
        this.dropSelf(ModBlocks.EMPTY_WINE_GLASS.get());
        this.dropSelf(ModBlocks.BERRY_WINE_GLASS.get());
        this.dropSelf(ModBlocks.CACTUS_WINE_GLASS.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(d -> (Block) d.get()).toList();
    }
}
