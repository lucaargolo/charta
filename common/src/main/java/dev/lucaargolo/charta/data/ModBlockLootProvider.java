package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.data.builder.ModBlockLootBuilder;

public class ModBlockLootProvider {

    public static void generate(ModBlockLootBuilder builder) {
        builder.dropSelf(ModBlocks.DEALER_TABLE.get());
        ModBlocks.CARD_TABLE_MAP.values().forEach(holder -> {
            builder.dropSelf(holder.get());
        });
        ModBlocks.GAME_CHAIR_MAP.values().forEach(holder -> {
            builder.dropSelf(holder.get());
        });
        ModBlocks.BAR_STOOL_MAP.values().forEach(holder -> {
            builder.dropSelf(holder.get());
        });
        ModBlocks.BAR_SHELF_MAP.values().forEach(holder -> {
            builder.dropSelf(holder.get());
        });
        builder.dropSelf(ModBlocks.EMPTY_BEER_GLASS.get());
        builder.dropSelf(ModBlocks.WHEAT_BEER_GLASS.get());
        builder.dropSelf(ModBlocks.SORGHUM_BEER_GLASS.get());
        builder.dropSelf(ModBlocks.EMPTY_WINE_GLASS.get());
        builder.dropSelf(ModBlocks.BERRY_WINE_GLASS.get());
        builder.dropSelf(ModBlocks.CACTUS_WINE_GLASS.get());
    }

}
