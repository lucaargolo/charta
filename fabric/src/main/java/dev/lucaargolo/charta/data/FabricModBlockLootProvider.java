package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.data.builder.ModBlockLootBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class FabricModBlockLootProvider extends FabricBlockLootTableProvider implements ModBlockLootBuilder {

    protected FabricModBlockLootProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        ModBlockLootProvider.generate(this);
    }

}
