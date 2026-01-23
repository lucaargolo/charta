package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class FabricModBlockRegistry extends ModBlockRegistry {

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    @Override
    public <E extends Block> BlockEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        BlockEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends Block> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(BuiltInRegistries.BLOCK, entry.key(), entry.get()));
    }

}