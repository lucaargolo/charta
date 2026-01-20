package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.data.fabric.FabricLikeDataOutput;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = ChartaMod.MOD_ID)
public class ChartaModData {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper exFileHelper = event.getExistingFileHelper();
        DatapackBuiltinEntriesProvider builtinProvider = new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), bootstrapRegistries(), Set.of(ChartaMod.MOD_ID));
        generator.addProvider(true, builtinProvider);
        generator.addProvider(event.includeClient(), new NeoForgeModModelProvider(new FabricLikeDataOutput(output.getOutputFolder(), event.validate())));
        generator.addProvider(event.includeServer(), NeoForgeModTagProvider.item(output, event.getLookupProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), NeoForgeModTagProvider.block(output, event.getLookupProvider(), exFileHelper));
        generator.addProvider(event.includeServer(), new NeoForgeModLootProvider(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ModLootProvider(output, builtinProvider.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new SuitImageProvider(output));
        generator.addProvider(event.includeServer(), new CardImageProvider(output));
        generator.addProvider(event.includeServer(), new DeckImageProvider(output));
        generator.addProvider(event.includeServer(), new DeckProvider(output));
        generator.addProvider(event.includeServer(), new ModBannerPatternTagsProvider(output, builtinProvider.getRegistryProvider()));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, builtinProvider.getRegistryProvider()));
    }

    public static RegistrySetBuilder bootstrapRegistries() {
        return new RegistrySetBuilder();
    }

}
