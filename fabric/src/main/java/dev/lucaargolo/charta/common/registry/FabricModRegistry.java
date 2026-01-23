package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

public class FabricModRegistry<T> extends ModRegistry<T> {

    private final Registry<T> registry;

    @SuppressWarnings("unchecked")
    public FabricModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
        if(registryKey.location().getNamespace().equals(ChartaMod.MOD_ID)) {
            this.registry = FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
        }else{
            this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
        }
    }

    @Override
    public void init() {
        this.entries.forEach(this::registerEntry);
    }

    @Override
    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public <E extends T> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags) {
        MinecraftEntry<E> entry = this.entry(path, supplier, tags);
        entries.put(path, entry);
        return entry;
    }

    private <E extends T> void registerEntry(String path, MinecraftEntry<E> entry) {
        entry.set(Registry.register(registry, entry.key(), entry.get()));
    }

}