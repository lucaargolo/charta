package dev.lucaargolo.charta.client.data;

import dev.lucaargolo.charta.client.data.fabric.FabricLikeDataOutput;
import dev.lucaargolo.charta.client.data.fabric.FabricLikeModelProvider;
import dev.lucaargolo.charta.data.ModBlockModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

public class NeoForgeModModelProvider extends FabricLikeModelProvider {

    public NeoForgeModModelProvider(FabricLikeDataOutput output) {
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
