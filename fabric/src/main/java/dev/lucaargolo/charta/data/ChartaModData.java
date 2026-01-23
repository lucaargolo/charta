package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.common.block.ModBannerPatterns;
import dev.lucaargolo.charta.data.image.CardImageProvider;
import dev.lucaargolo.charta.data.image.DeckImageProvider;
import dev.lucaargolo.charta.data.image.SuitImageProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ChartaModData implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(BuiltinProvider::new);

        pack.addProvider(FabricModModelProvider::new);

        pack.addProvider(SuitImageProvider::new);
        pack.addProvider(CardImageProvider::new);
        pack.addProvider(DeckImageProvider::new);

        pack.addProvider(ModDeckProvider::new);
        pack.addProvider(ModRecipeProvider::new);

        pack.addProvider(FabricModBlockLootProvider::new);
        pack.addProvider(FabricModChestLootProvider::new);

        pack.addProvider(FabricModTagProvider::item);
        pack.addProvider(FabricModTagProvider::block);
        pack.addProvider(ModBannerPatternTagsProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }

    private static class BuiltinProvider extends FabricDynamicRegistryProvider {

        public BuiltinProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> provider) {
            super(output, provider);
        }

        @Override
        protected void configure(HolderLookup.Provider provider, FabricDynamicRegistryProvider.Entries entries) {
            entries.addAll(provider.lookupOrThrow(Registries.BANNER_PATTERN));
        }

        @Override
        public @NotNull String getName() {
            return "Bootstrap";
        }

    }

}
