package dev.lucaargolo.charta.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;

import java.util.HashMap;
import java.util.Map;

public class DyeColorHelper {

    private static final Map<DyeColor, Block> COLOR_CARPETS = new HashMap<>();

    public static Block getCarpet(DyeColor color) {
        if(COLOR_CARPETS.isEmpty()) {
            BuiltInRegistries.BLOCK.forEach(block -> {
                if(block instanceof WoolCarpetBlock carpetBlock) {
                    COLOR_CARPETS.put(carpetBlock.getColor(), carpetBlock);
                }
            });
        }
        return COLOR_CARPETS.getOrDefault(color, Blocks.WHITE_CARPET);
    }

}
