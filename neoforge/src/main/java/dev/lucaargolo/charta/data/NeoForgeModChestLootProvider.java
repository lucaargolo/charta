package dev.lucaargolo.charta.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public class NeoForgeModChestLootProvider implements LootTableSubProvider {

    public NeoForgeModChestLootProvider(HolderLookup.Provider provider) {

    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        ModChestLootProvider.generate(output);
    }


}
