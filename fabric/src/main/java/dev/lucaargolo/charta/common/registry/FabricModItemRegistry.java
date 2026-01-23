package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class FabricModItemRegistry extends ModItemRegistry {

    @Override
    public void init() {
        entries.forEach(this::registerEntry);
    }

    @Override
    public <E extends Item> ItemEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        ItemEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends Item> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(BuiltInRegistries.ITEM, entry.key(), entry.get()));
    }

}