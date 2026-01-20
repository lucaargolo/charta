package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBannerPatterns;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = Charta.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();
        DatapackBuiltinEntriesProvider builtinProvider = new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), bootstrapRegistries(), Set.of(Charta.MOD_ID));
        generator.addProvider(true, builtinProvider);
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, exFileHelper));
        generator.addProvider(event.includeServer(), new SuitImageProvider(output));
        generator.addProvider(event.includeServer(), new CardImageProvider(output));
        generator.addProvider(event.includeServer(), new DeckImageProvider(output));
        generator.addProvider(event.includeServer(), new DeckProvider(output));
        generator.addProvider(event.includeServer(), new ModLootProvider(output, builtinProvider.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new ModBannerPatternTagsProvider(output, builtinProvider.getRegistryProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, builtinProvider.getRegistryProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, builtinProvider.getRegistryProvider()));
    }

    public static RegistrySetBuilder bootstrapRegistries() {
        return new RegistrySetBuilder()
            .add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }

}
