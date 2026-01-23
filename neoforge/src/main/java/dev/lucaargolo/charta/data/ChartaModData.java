package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.block.ModBannerPatterns;
import dev.lucaargolo.charta.data.fabric.FabricLikeDataOutput;
import dev.lucaargolo.charta.data.image.CardImageProvider;
import dev.lucaargolo.charta.data.image.DeckImageProvider;
import dev.lucaargolo.charta.data.image.SuitImageProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = ChartaMod.MOD_ID)
public class ChartaModData {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator pack = event.getGenerator();
        DatapackBuiltinEntriesProvider builtinProvider = pack.addProvider(true, new DatapackBuiltinEntriesProvider(pack.getPackOutput(), event.getLookupProvider(), buildRegistry(), Set.of(ChartaMod.MOD_ID)));

        pack.addProvider(event.includeClient(), new NeoForgeModModelProvider(new FabricLikeDataOutput(pack.getPackOutput().getOutputFolder(), event.validate())));

        pack.addProvider(event.includeServer(), new SuitImageProvider(pack.getPackOutput(), event.getLookupProvider()));
        pack.addProvider(event.includeServer(), new CardImageProvider(pack.getPackOutput(), event.getLookupProvider()));
        pack.addProvider(event.includeServer(), new DeckImageProvider(pack.getPackOutput(), event.getLookupProvider()));

        pack.addProvider(event.includeServer(), new ModDeckProvider(pack.getPackOutput(), event.getLookupProvider()));
        pack.addProvider(event.includeServer(), new ModRecipeProvider(pack.getPackOutput(), builtinProvider.getRegistryProvider()));

        pack.addProvider(event.includeServer(), new NeoForgeModLootProvider(pack.getPackOutput(), event.getLookupProvider()));

        pack.addProvider(event.includeServer(), NeoForgeModTagProvider.item(pack.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        pack.addProvider(event.includeServer(), NeoForgeModTagProvider.block(pack.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        pack.addProvider(event.includeServer(), new ModBannerPatternTagsProvider(pack.getPackOutput(), builtinProvider.getRegistryProvider()));
    }

    private static RegistrySetBuilder buildRegistry() {
        return new RegistrySetBuilder().add(Registries.BANNER_PATTERN, ModBannerPatterns::bootstrap);
    }

}
