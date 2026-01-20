package dev.lucaargolo.charta.data.builder;

import net.minecraft.resources.ResourceLocation;

public interface ModTagBuilder<T> {

    ModTagBuilder<T> add(T entry);

    ModTagBuilder<T> add(ResourceLocation entryLocation);

    ModTagBuilder<T> addOptional(ResourceLocation entryLocation);

    ModTagBuilder<T> addTag(ResourceLocation tagLocation);

    ModTagBuilder<T> addOptionalTag(ResourceLocation tagLocation);

    ModTagBuilder<T> setReplace(boolean value);

}
