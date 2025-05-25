package dev.lucaargolo.charta.game;

import com.mojang.serialization.DataResult;
import dev.lucaargolo.charta.Charta;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;

public record Rank(ResourceLocation location, int ordinal) implements Comparable<Rank> {

    private static final LinkedHashSet<Rank> registry = new LinkedHashSet<>();

    public static final StreamCodec<ByteBuf, Rank> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, Rank::location, l -> Rank.load(l).getOrThrow());

    public Rank {
        for(char c : location.toString().toCharArray()) {
            if(c == '_' || c == '-' || c == '/' || c == '.') {
                throw new IllegalStateException("Non [a-z0-9] character in rank location: " + location);
            }
        }
        if(!registry.add(this)) {
            throw new IllegalStateException("Duplicate rank: " + location);
        }
    }

    public static DataResult<Rank> load(ResourceLocation location) {
        Optional<Rank> rank = registry.stream().filter(r -> r.location.equals(location)).findFirst();
        return rank.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No such rank: " + location));
    }

    public static final Rank BLANK = new Rank(Charta.id("blank"), 0);
    public static final Rank ACE = new Rank(Charta.id("ace"), 1);
    public static final Rank TWO = new Rank(Charta.id("two"), 2);
    public static final Rank THREE = new Rank(Charta.id("three"), 3);
    public static final Rank FOUR = new Rank(Charta.id("four"), 4);
    public static final Rank FIVE = new Rank(Charta.id("five"), 5);
    public static final Rank SIX = new Rank(Charta.id("six"), 6);
    public static final Rank SEVEN = new Rank(Charta.id("seven"), 7);
    public static final Rank EIGHT = new Rank(Charta.id("eight"), 8);
    public static final Rank NINE = new Rank(Charta.id("nine"), 9);
    public static final Rank TEN = new Rank(Charta.id("ten"), 10);
    public static final Rank JACK = new Rank(Charta.id("jack"), 11);
    public static final Rank QUEEN = new Rank(Charta.id("queen"), 12);
    public static final Rank KING = new Rank(Charta.id("king"), 13);
    public static final Rank JOKER = new Rank(Charta.id("joker"), 14);

    public static final Rank WILD = new Rank(Charta.id("wild"), -1);
    public static final Rank ZERO = new Rank(Charta.id("zero"), 0);
    public static final Rank ONE = new Rank(Charta.id("one"), 1);
    public static final Rank BLOCK = new Rank(Charta.id("block"), 10);
    public static final Rank REVERSE = new Rank(Charta.id("reverse"), 11);
    public static final Rank PLUS_2 = new Rank(Charta.id("plustwo"), 13);
    public static final Rank WILD_PLUS_4 = new Rank(Charta.id("wildplusfour"), 14);

    @Override
    public String toString() {
        return this.location.toString();
    }

    @Override
    public int compareTo(@NotNull Rank other) {
        return this.ordinal - other.ordinal;
    }

}
