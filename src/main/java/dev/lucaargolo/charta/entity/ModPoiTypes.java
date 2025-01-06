package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;

public class ModPoiTypes {

    public static void register() {
        PointOfInterestHelper.register(Charta.id("dealer"), 1, 1, ModBlocks.DEALER_TABLE);
    }
    
}
