package dev.lucaargolo.charta.common.registry;

import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

public abstract class ModRegistry<T> extends MinecraftRegistry<T, MinecraftEntry<? extends T>> {

    public ModRegistry(ResourceKey<Registry<T>> registryKey) {
        super(registryKey);
    }

    @Override
    public abstract <E extends T> MinecraftEntry<E> register(String path, Supplier<E> supplier, TagKey<?>... tags);

    @Override
    protected <E extends T> MinecraftEntry<E> entry(String path, Supplier<E> supplier, TagKey<?>... tags) {
        return new MinecraftEntry<>(path, supplier, tags);
    }

}