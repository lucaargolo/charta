package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootProvider extends FabricBlockLootTableProvider {

    protected ModBlockLootProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.dropSelf(ModBlocks.DEALER_TABLE);
        ModBlocks.CARD_TABLE_MAP.values().forEach(this::dropSelf);
        ModBlocks.GAME_CHAIR_MAP.values().forEach(this::dropSelf);
        ModBlocks.BAR_STOOL_MAP.values().forEach(this::dropSelf);
        ModBlocks.BAR_SHELF_MAP.values().forEach(this::dropSelf);
        this.dropSelf(ModBlocks.EMPTY_BEER_GLASS);
        this.dropSelf(ModBlocks.WHEAT_BEER_GLASS);
        this.dropSelf(ModBlocks.SORGHUM_BEER_GLASS);
        this.dropSelf(ModBlocks.EMPTY_WINE_GLASS);
        this.dropSelf(ModBlocks.BERRY_WINE_GLASS);
        this.dropSelf(ModBlocks.CACTUS_WINE_GLASS);
    }

}
