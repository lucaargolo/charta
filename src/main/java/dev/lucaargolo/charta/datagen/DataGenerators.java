package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Set;

@EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
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
        generator.addProvider(event.includeServer(), new CardDeckProvider(output));
        //TODO: This
        //generator.addProvider(event.includeServer(), new ModLootProvider(output));
        //generator.addProvider(event.includeServer(), new ModBannerPatternTagsProvider(output, builtinProvider.getRegistryProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, builtinProvider.getRegistryProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
    }

    public static RegistrySetBuilder bootstrapRegistries() {
        return new RegistrySetBuilder();
            //.add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }

}
