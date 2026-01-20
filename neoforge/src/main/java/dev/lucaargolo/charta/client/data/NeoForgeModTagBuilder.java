package dev.lucaargolo.charta.client.data;

import dev.lucaargolo.furniture.data.builder.ModTagBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;

import java.util.Optional;

public class NeoForgeModTagBuilder<T> implements ModTagBuilder<T> {

    private final ResourceKey<? extends Registry<T>> registry;
    private final TagBuilder wrapped;

    public NeoForgeModTagBuilder(ResourceKey<? extends Registry<T>> registry, TagBuilder wrapped) {
        this.registry = registry;
        this.wrapped = wrapped;
    }

    @Override
    public ModTagBuilder<T> add(T entry) {
        this.wrapped.add(TagEntry.element(reverseLookup(entry).location()));
        return this;
    }

    @Override
    public ModTagBuilder<T> add(ResourceLocation entryLocation) {
        this.wrapped.addElement(entryLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> addOptional(ResourceLocation entryLocation) {
        this.wrapped.addOptionalElement(entryLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> addTag(ResourceLocation tagLocation) {
        this.wrapped.addTag(tagLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> addOptionalTag(ResourceLocation tagLocation) {
        this.wrapped.addOptionalTag(tagLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> setReplace(boolean value) {
        this.wrapped.replace(value);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResourceKey<T> reverseLookup(T element) {
        Registry registry = BuiltInRegistries.REGISTRY.get(this.registry.location());
        if (registry != null) {
            Optional<Holder<T>> key = registry.getResourceKey(element);
            if (key.isPresent()) {
                return (ResourceKey<T>) key.get();
            }
        }

        throw new UnsupportedOperationException("Adding objects is not supported by " + this.getClass());
    }


}
