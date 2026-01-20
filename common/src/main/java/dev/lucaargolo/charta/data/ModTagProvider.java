package dev.lucaargolo.charta.data;

import dev.lucaargolo.charta.data.builder.ModTagBuilder;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftRegistry;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class ModTagProvider {

    public static <T, M extends MinecraftEntry<? extends T>> void generate(MinecraftRegistry<T, M> registry, Function<TagKey<T>, ModTagBuilder<T>> function) {
        registry.getEntries().forEach((entry) -> {
            Arrays.stream(entry.getTags()).map(t -> t.cast(registry.getRegistryKey())).filter(Optional::isPresent).map(Optional::get).forEach(tag -> {
                function.apply(tag).add(entry.get()).setReplace(false);
            });
        });
    }

}
