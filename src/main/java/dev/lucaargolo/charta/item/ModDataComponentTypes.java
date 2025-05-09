package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredHolder;
import net.minecraftforge.registries.DeferredRegister;

public class ModDataComponentTypes {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Charta.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> CARD_DECK = DATA_COMPONENT_TYPES.register("deck_id", () -> new DataComponentType.Builder<ResourceLocation>().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding().build());

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }


}
