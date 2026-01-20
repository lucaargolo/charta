package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.registry.ModRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class ModDataComponentTypes {

    public static final ModRegistry<DataComponentType<?>> REGISTRY = ChartaMod.registry(Registries.DATA_COMPONENT_TYPE);

    public static final MinecraftEntry<DataComponentType<ResourceLocation>> CARD_DECK = REGISTRY.register("deck_id", () -> new DataComponentType.Builder<ResourceLocation>().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding().build());

}
