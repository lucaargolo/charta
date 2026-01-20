package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.ModBlocks;
import dev.lucaargolo.charta.item.ModItems;
import dev.lucaargolo.charta.registry.ModBlockRegistry;
import dev.lucaargolo.charta.registry.ModItemRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class NeoForgeModTagProvider<T, M extends MinecraftEntry<? extends T>> extends TagsProvider<T> {

    private final MinecraftRegistry<T, M> registry;

    protected NeoForgeModTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper, MinecraftRegistry<T, M> registry) {
        super(output, registry.getRegistryKey(), lookupProvider, ChartaMod.MOD_ID, existingFileHelper);
        this.registry = registry;
    }


    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        ModTagProvider.generate(registry, tag -> new NeoForgeModTagBuilder<>(registryKey, this.getOrCreateRawBuilder(tag)));
    }

    public static NeoForgeModTagProvider<Block, ModBlockRegistry.BlockEntry<?>> block(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new NeoForgeModTagProvider<>(output, lookupProvider, existingFileHelper, ModBlocks.REGISTRY);
    }

    public static NeoForgeModTagProvider<Item, ModItemRegistry.ItemEntry<?>> item(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new NeoForgeModTagProvider<>(output, lookupProvider, existingFileHelper, ModItems.REGISTRY);
    }

}
