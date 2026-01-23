package dev.lucaargolo.charta.common.registry.minecraft;

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

    protected MinecraftRegistry(ResourceKey<Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public abstract void init();

    public abstract Registry<T> getRegistry();

    public ResourceKey<Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    @Nullable
    public M get(String path) {
        return this.entries.get(path);
    }

    public Collection<M> getEntries() {
        return this.entries.values();
    }

    public <E extends T> E registerDirectly(String path, Supplier<E> supplier, TagKey<?>... tags) {
        E value = supplier.get();
        register(path, () -> value, tags);
        return value;
    }

    public abstract <E extends T> M register(String path, Supplier<E> supplier, TagKey<?>... tags);

    protected abstract <E extends T> M entry(String path, Supplier<E> supplier, TagKey<?>... tags);

}
