package dev.lucaargolo.charta.common.game;

import com.mojang.serialization.Codec;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.card.Suit;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class Suits {

    public static final ResourceKey<Registry<Suit>> REGISTRY_KEY = ResourceKey.createRegistryKey(ChartaMod.id("suit"));
    public static final ModRegistry<Suit> MOD_REGISTRY = ChartaMod.registry(REGISTRY_KEY);

    public static final Suit BLANK = MOD_REGISTRY.registerDirectly("blank", Suit::new);

    public static final Suit SPADES = MOD_REGISTRY.registerDirectly("spades", Suit::new);
    public static final Suit HEARTS = MOD_REGISTRY.registerDirectly("hearts", Suit::new);
    public static final Suit CLUBS = MOD_REGISTRY.registerDirectly("clubs", Suit::new);
    public static final Suit DIAMONDS = MOD_REGISTRY.registerDirectly("diamonds", Suit::new);
    public static final Set<Suit> STANDARD = Set.of(SPADES, HEARTS, CLUBS, DIAMONDS);

    public static final Suit RED = MOD_REGISTRY.registerDirectly("red", Suit::new);
    public static final Suit YELLOW = MOD_REGISTRY.registerDirectly("yellow", Suit::new);
    public static final Suit GREEN = MOD_REGISTRY.registerDirectly("green", Suit::new);
    public static final Suit BLUE = MOD_REGISTRY.registerDirectly("blue", Suit::new);
    public static final Set<Suit> FUN = Set.of(Suits.RED, Suits.YELLOW, Suits.GREEN, Suits.BLUE);

    public static Registry<Suit> getRegistry() {
        return MOD_REGISTRY.getRegistry();
    }

    public static ResourceLocation getLocation(Suit suit) {
        return getRegistry().getKey(suit);
    }

    public static Codec<Suit> getCodec() {
        return Suits.getRegistry().byNameCodec();
    }

    public static StreamCodec<ByteBuf, Suit> getStreamCodec() {
        return ByteBufCodecs.fromCodec(getCodec());
    }

}
