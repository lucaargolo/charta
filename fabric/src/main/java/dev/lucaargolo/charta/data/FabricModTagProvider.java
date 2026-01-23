package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.common.block.ModBlocks;
import dev.lucaargolo.charta.common.item.ModItems;
import dev.lucaargolo.charta.common.registry.ModBlockRegistry;
import dev.lucaargolo.charta.common.registry.ModItemRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FabricModTagProvider<T, M extends MinecraftEntry<? extends T>> extends FabricTagProvider<T> {

    private final MinecraftRegistry<T, M> registry;

    public FabricModTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, MinecraftRegistry<T, M> registry) {
        super(output, registry.getRegistryKey(), registriesFuture);
        this.registry = registry;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ModTagProvider.generate(registry, tag -> new FabricModTagBuilder<>(this.registryKey, this.getOrCreateTagBuilder(tag)));
    }

    public static FabricModTagProvider<Block, ModBlockRegistry.BlockEntry<?>> block(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        return new FabricModTagProvider<>(output, registriesFuture, ModBlocks.REGISTRY);
    }

    public static FabricModTagProvider<Item, ModItemRegistry.ItemEntry<?>> item(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        return new FabricModTagProvider<>(output, registriesFuture, ModItems.REGISTRY);
    }

}
