package dev.lucaargolo.charta.registry.minecraft;

import dev.lucaargolo.charta.ChartaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

public class MinecraftEntry<E> implements Supplier<E> {

    private final int localId;
    private final String path;
    private final Supplier<E> supplier;
    private final TagKey<?>[] tags;

    private boolean supplied = false;
    private E value;

    public MinecraftEntry(int localId, String path, Supplier<E> supplier, TagKey<?>... tags) {
        this.localId = localId;
        this.path = path;
        this.supplier = supplier;
        this.tags = tags;
    }

    @Override
    public E get() {
        return supplied ? value : supplier.get();
    }

    public void set(E value) {
        this.supplied = true;
        this.value = value;
    }

    public int getLocalId() {
        return localId;
    }

    public TagKey<?>[] getTags() {
        return tags;
    }

    public String path() {
        return this.path;
    }

    public ResourceLocation key() {
        return ChartaMod.id(path);
    }

}
