package dev.lucaargolo.charta.client.data.fabric;

import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.ModelProvider;

public abstract class FabricLikeModelProvider extends ModelProvider {

	public FabricLikeModelProvider(PackOutput output) {
		super(output);
	}

	public abstract void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator);

	public abstract void generateItemModels(ItemModelGenerators itemModelGenerator);

}
