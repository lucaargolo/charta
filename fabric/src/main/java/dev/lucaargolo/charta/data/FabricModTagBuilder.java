package dev.lucaargolo.charta.data;

import dev.lucaargolo.furniture.data.builder.ModTagBuilder;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class FabricModTagBuilder<T> implements ModTagBuilder<T> {

    private final ResourceKey<? extends Registry<T>> registry;
    private final FabricTagProvider<T>.FabricTagBuilder wrapped;

    public FabricModTagBuilder(ResourceKey<? extends Registry<T>> registry, FabricTagProvider<T>.FabricTagBuilder wrapped) {
        this.registry = registry;
        this.wrapped = wrapped;
    }

    @Override
    public ModTagBuilder<T> add(T entry) {
        this.wrapped.add(entry);
        return this;
    }

    @Override
    public ModTagBuilder<T> add(ResourceLocation entryLocation) {
        this.wrapped.add(entryLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> addOptional(ResourceLocation entryLocation) {
        this.wrapped.addOptional(entryLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> addTag(ResourceLocation tagLocation) {
        this.wrapped.addTag(TagKey.create(registry, tagLocation));
        return this;
    }

    @Override
    public ModTagBuilder<T> addOptionalTag(ResourceLocation tagLocation) {
        this.wrapped.addOptionalTag(tagLocation);
        return this;
    }

    @Override
    public ModTagBuilder<T> setReplace(boolean value) {
        this.wrapped.setReplace(value);
        return this;
    }


}
