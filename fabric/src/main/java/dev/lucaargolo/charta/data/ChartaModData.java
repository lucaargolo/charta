package dev.lucaargolo.charta.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ChartaModData implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(FabricModModelProvider::new);
        pack.addProvider(FabricModTagProvider::item);
        pack.addProvider(FabricModTagProvider::block);
        pack.addProvider(FabricModBlockLootProvider::new);
        pack.addProvider(ModLootProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<SuitImageProvider>) SuitImageProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<CardImageProvider>) CardImageProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<DeckImageProvider>) DeckImageProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<DeckProvider>) DeckProvider::new);
        pack.addProvider(ModBannerPatternTagsProvider::new);
        pack.addProvider(ModRecipeProvider::new);
    }

}
