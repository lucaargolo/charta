package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.block.ModBannerPatterns;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class DataGenerators implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModDynamicRegistryProvider::new);
        pack.addProvider(ModBlockStateProvider::new);
        pack.addProvider(SuitImageProvider::new);
        pack.addProvider(CardImageProvider::new);
        pack.addProvider(DeckImageProvider::new);
        pack.addProvider(CardDeckProvider::new);
        pack.addProvider(ModBlockLootProvider::new);
        pack.addProvider(ModLootProvider::new);
        pack.addProvider(ModBannerPatternTagsProvider::new);
        pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider(ModRecipeProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }

}
