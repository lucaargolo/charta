package dev.lucaargolo.charta.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NeoForgeModLootProvider extends LootTableProvider {

    public NeoForgeModLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(NeoForgeModBlockLootProvider::new, LootContextParamSets.BLOCK),
            new SubProviderEntry(NeoForgeModChestLootProvider::new, LootContextParamSets.CHEST)
        ), registries);
    }

}
