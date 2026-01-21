package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.block.ModBannerPatterns;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public class ChartaModData implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();

        pack.addProvider(FabricModModelProvider::new);

        pack.addProvider(SuitImageProvider::new);
        pack.addProvider(CardImageProvider::new);
        pack.addProvider(DeckImageProvider::new);

        pack.addProvider(DeckProvider::new);
        pack.addProvider(ModRecipeProvider::new);

        pack.addProvider(ModLootProvider::new);
        pack.addProvider(FabricModBlockLootProvider::new);

        pack.addProvider(FabricModTagProvider::item);
        pack.addProvider(FabricModTagProvider::block);

        pack.addProvider(ModBannerPatternTagsProvider::new);
        pack.addProvider((output, provider) -> {
            return new FabricDynamicRegistryProvider(output, provider) {
                @Override
                protected void configure(HolderLookup.Provider provider, Entries entries) {
                    entries.addAll(provider.lookupOrThrow(Registries.BANNER_PATTERN));
                }

                @Override
                public @NotNull String getName() {
                    return "Bootstrap";
                }
            };
        });
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }
}
