package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModDataComponentTypes {

    public static final Map<ResourceLocation, DataComponentType<?>> DATA_COMPONENT_TYPES = new HashMap<>();

    public static final DataComponentType<ResourceLocation> CARD_DECK = register("deck_id", () -> new DataComponentType.Builder<ResourceLocation>().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding().build());

    private static <M, T extends DataComponentType<M>> T register(String id, Supplier<T> container) {
        T obj = container.get();
        DATA_COMPONENT_TYPES.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        DATA_COMPONENT_TYPES.forEach((id, container) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, container));
    }

}
