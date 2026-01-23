package dev.lucaargolo.charta.common.data;

import com.mojang.serialization.Codec;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.Ranks;
import dev.lucaargolo.charta.common.game.Suits;
import dev.lucaargolo.charta.common.game.api.card.Rank;
import dev.lucaargolo.charta.common.game.api.card.Suit;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public class ModDataComponentTypes {

    public static final ModRegistry<DataComponentType<?>> REGISTRY = ChartaMod.registry(Registries.DATA_COMPONENT_TYPE);

    public static final MinecraftEntry<DataComponentType<ResourceLocation>> DECK = REGISTRY.register("deck_id", () -> new DataComponentType.Builder<ResourceLocation>().persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding().build());

    public static final MinecraftEntry<DataComponentType<Suit>> SUIT = REGISTRY.register("suit", () -> new DataComponentType.Builder<Suit>().persistent(Suits.getCodec()).networkSynchronized(Suits.getStreamCodec()).cacheEncoding().build());
    public static final MinecraftEntry<DataComponentType<Rank>> RANK = REGISTRY.register("rank", () -> new DataComponentType.Builder<Rank>().persistent(Ranks.getCodec()).networkSynchronized(Ranks.getStreamCodec()).cacheEncoding().build());
    public static final MinecraftEntry<DataComponentType<Boolean>> FLIPPED = REGISTRY.register("flipped", () -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).cacheEncoding().build());

}
