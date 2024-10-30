package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModEntityDataSerializers {

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Charta.MOD_ID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<List<Card>>> CARD_LIST = ENTITY_DATA_SERIALIZERS.register("card_list", () -> new EntityDataSerializer<>() {
        @Override
        public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, List<Card>> codec() {
            return Card.LIST_STREAM_CODEC;
        }

        @Override
        public @NotNull List<Card> copy(@NotNull List<Card> value) {
            return List.copyOf(value);
        }
    });

    public static void register(IEventBus bus) {
        ENTITY_DATA_SERIALIZERS.register(bus);
    }

}
