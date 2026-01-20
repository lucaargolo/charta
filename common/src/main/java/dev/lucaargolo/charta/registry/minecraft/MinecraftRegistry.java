package dev.lucaargolo.charta.registry.minecraft;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class MinecraftRegistry<T, M extends MinecraftEntry<? extends T>> {

    protected final ResourceKey<Registry<T>> registryKey;
    protected final Map<String, M> entries = new LinkedHashMap<>();

    protected int id = 0;

    protected MinecraftRegistry(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public abstract void init();

    @Nullable
    public M get(String path) {
        return entries.get(path);
    }

    public Collection<M> getEntries() {
        return entries.values();
    }

    public abstract <E extends T> M register(String path, Supplier<E> supplier, TagKey<?>... tags);

    protected abstract <E extends T> M entry(String path, Supplier<E> supplier, TagKey<?>... tags);

    public ResourceKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

}
