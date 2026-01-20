package dev.lucaargolo.charta.game;

import com.mojang.serialization.DataResult;
import dev.lucaargolo.charta.ChartaMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Optional;

public record Rank(ResourceLocation location, int ordinal) implements Comparable<Rank> {

    private static final LinkedHashSet<Rank> registry = new LinkedHashSet<>();
    public static final StreamCodec<ByteBuf, Rank> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, Rank::location, l -> Rank.load(l).getOrThrow());

    public static final Rank BLANK = new Rank(ChartaMod.id("blank"), 0);
    public static final Rank ACE = new Rank(ChartaMod.id("ace"), 1);
    public static final Rank TWO = new Rank(ChartaMod.id("two"), 2);
    public static final Rank THREE = new Rank(ChartaMod.id("three"), 3);
    public static final Rank FOUR = new Rank(ChartaMod.id("four"), 4);
    public static final Rank FIVE = new Rank(ChartaMod.id("five"), 5);
    public static final Rank SIX = new Rank(ChartaMod.id("six"), 6);
    public static final Rank SEVEN = new Rank(ChartaMod.id("seven"), 7);
    public static final Rank EIGHT = new Rank(ChartaMod.id("eight"), 8);
    public static final Rank NINE = new Rank(ChartaMod.id("nine"), 9);
    public static final Rank TEN = new Rank(ChartaMod.id("ten"), 10);
    public static final Rank JACK = new Rank(ChartaMod.id("jack"), 11);
    public static final Rank QUEEN = new Rank(ChartaMod.id("queen"), 12);
    public static final Rank KING = new Rank(ChartaMod.id("king"), 13);
    public static final Rank JOKER = new Rank(ChartaMod.id("joker"), 14);

    public static final Rank WILD = new Rank(ChartaMod.id("wild"), -1);
    public static final Rank ZERO = new Rank(ChartaMod.id("zero"), 0);
    public static final Rank ONE = new Rank(ChartaMod.id("one"), 1);
    public static final Rank BLOCK = new Rank(ChartaMod.id("block"), 10);
    public static final Rank REVERSE = new Rank(ChartaMod.id("reverse"), 11);
    public static final Rank PLUS_2 = new Rank(ChartaMod.id("plustwo"), 13);
    public static final Rank WILD_PLUS_4 = new Rank(ChartaMod.id("wildplusfour"), 14);

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

    @Override
    public String toString() {
        return this.location.toString();
    }

    @Override
    public int compareTo(@NotNull Rank other) {
        return this.ordinal - other.ordinal;
    }

}
