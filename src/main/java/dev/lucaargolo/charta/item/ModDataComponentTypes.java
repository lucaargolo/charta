package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardDeck;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponentTypes {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Charta.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardDeck>> CARD_DECK = DATA_COMPONENT_TYPES.register("card_deck", () -> new DataComponentType.Builder<CardDeck>().persistent(CardDeck.CODEC).networkSynchronized(CardDeck.STREAM_CODEC).cacheEncoding().build());

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }


}
