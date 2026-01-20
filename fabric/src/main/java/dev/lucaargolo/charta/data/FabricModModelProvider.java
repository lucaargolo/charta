package dev.lucaargolo.charta.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.TextureSlot;

public class FabricModModelProvider extends FabricModelProvider {

    private static final TextureSlot LOG = TextureSlot.create("log");
    private static final TextureSlot PLANKS = TextureSlot.create("planks");
    private static final TextureSlot DOORS = TextureSlot.create("doors");
    private static final TextureSlot STONE = TextureSlot.create("stone");
    private static final TextureSlot METAL = TextureSlot.create("metal");

    public FabricModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        ModBlockModelProvider.generate(generators);
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {

    }

}
