package dev.lucaargolo.charta.common.game;

import com.mojang.serialization.Codec;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.card.Rank;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class Ranks {

    public static final ResourceKey<Registry<Rank>> REGISTRY_KEY = ResourceKey.createRegistryKey(ChartaMod.id("rank"));
    public static final ModRegistry<Rank> MOD_REGISTRY = ChartaMod.registry(REGISTRY_KEY);

    public static final Rank BLANK = MOD_REGISTRY.registerDirectly("blank", () -> new Rank(0));
    public static final Rank ACE = MOD_REGISTRY.registerDirectly("ace", () -> new Rank(1));
    public static final Rank TWO = MOD_REGISTRY.registerDirectly("two", () -> new Rank(2));
    public static final Rank THREE = MOD_REGISTRY.registerDirectly("three", () -> new Rank(3));
    public static final Rank FOUR = MOD_REGISTRY.registerDirectly("four", () -> new Rank(4));
    public static final Rank FIVE = MOD_REGISTRY.registerDirectly("five", () -> new Rank(5));
    public static final Rank SIX = MOD_REGISTRY.registerDirectly("six", () -> new Rank(6));
    public static final Rank SEVEN = MOD_REGISTRY.registerDirectly("seven", () -> new Rank(7));
    public static final Rank EIGHT = MOD_REGISTRY.registerDirectly("eight", () -> new Rank(8));
    public static final Rank NINE = MOD_REGISTRY.registerDirectly("nine", () -> new Rank(9));
    public static final Rank TEN = MOD_REGISTRY.registerDirectly("ten", () -> new Rank(10));
    public static final Rank JACK = MOD_REGISTRY.registerDirectly("jack", () -> new Rank(11));
    public static final Rank QUEEN = MOD_REGISTRY.registerDirectly("queen", () -> new Rank(12));
    public static final Rank KING = MOD_REGISTRY.registerDirectly("king", () -> new Rank(13));
    public static final Rank JOKER = MOD_REGISTRY.registerDirectly("joker", () -> new Rank(14));
    public static final Set<Rank> STANDARD = Set.of(ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING);

    public static final Rank WILD = MOD_REGISTRY.registerDirectly("wild", () -> new Rank(-1));
    public static final Rank ZERO = MOD_REGISTRY.registerDirectly("zero", () -> new Rank(0));
    public static final Rank ONE = MOD_REGISTRY.registerDirectly("one", () -> new Rank(1));
    public static final Rank BLOCK = MOD_REGISTRY.registerDirectly("block", () -> new Rank(10));
    public static final Rank REVERSE = MOD_REGISTRY.registerDirectly("reverse", () -> new Rank(11));
    public static final Rank PLUS_2 = MOD_REGISTRY.registerDirectly("plustwo", () -> new Rank(13));
    public static final Rank WILD_PLUS_4 = MOD_REGISTRY.registerDirectly("wildplusfour", () -> new Rank(14));
    public static final Set<Rank> FUN = Set.of(ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, ZERO, BLOCK, REVERSE, PLUS_2, WILD, WILD_PLUS_4);

    public static Registry<Rank> getRegistry() {
        return MOD_REGISTRY.getRegistry();
    }

    public static ResourceLocation getLocation(Rank rank) {
        return getRegistry().getKey(rank);
    }

    public static Codec<Rank> getCodec() {
        return Ranks.getRegistry().byNameCodec();
    }

    public static StreamCodec<ByteBuf, Rank> getStreamCodec() {
        return ByteBufCodecs.fromCodec(getCodec());
    }


}
