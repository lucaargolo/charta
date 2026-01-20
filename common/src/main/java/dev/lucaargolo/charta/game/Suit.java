package dev.lucaargolo.charta.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lucaargolo.charta.ChartaMod;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

public class Suit implements Comparable<Suit> {

    private static final LinkedHashSet<Suit> registry = new LinkedHashSet<>();
    private static final Int2ObjectMap<Suit> backing = new Int2ObjectArrayMap<>();

    public static final StreamCodec<ByteBuf, Suit> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, Suit::location, l -> Suit.load(l).getOrThrow());
    public static final Codec<Suit> CODEC = ResourceLocation.CODEC.comapFlatMap(Suit::load, r -> r.location);

    public static final Suit BLANK = new Suit(ChartaMod.id("blank"));

    public static final Suit SPADES = new Suit(ChartaMod.id("spades"));
    public static final Suit HEARTS = new Suit(ChartaMod.id("hearts"));
    public static final Suit CLUBS = new Suit(ChartaMod.id("clubs"));
    public static final Suit DIAMONDS = new Suit(ChartaMod.id("diamonds"));

    public static final Suit RED = new Suit(ChartaMod.id("red"));
    public static final Suit YELLOW = new Suit(ChartaMod.id("yellow"));
    public static final Suit GREEN = new Suit(ChartaMod.id("green"));
    public static final Suit BLUE = new Suit(ChartaMod.id("blue"));

    private final ResourceLocation location;
    private final int id;

    public Suit(ResourceLocation location) {
        for(char c : location.toString().toCharArray()) {
            if(c == '_' || c == '-' || c == '/' || c == '.') {
                throw new IllegalStateException("Non [a-z0-9] character in suit location: " + location);
            }
        }
        this.location = location;
        if(registry.add(this)) {
            this.id = registry.size();
            backing.put(id, this);
        }else{
            throw new IllegalStateException("Duplicate suit: " + location);
        }
    }

    public static DataResult<Suit> load(ResourceLocation location) {
        Optional<Suit> suit = registry.stream().filter(r -> r.location.equals(location)).findFirst();
        return suit.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No such suit: " + location));
    }

    public ResourceLocation location() {
        return location;
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Suit suit)) return false;
        return Objects.equals(location, suit.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public String toString() {
        return this.location.toString();
    }

    @Override
    public int compareTo(@NotNull Suit other) {
        return this.id - other.id;
    }

    @ApiStatus.Internal
    public static Suit getSuit(int ordinal) {
        return backing.getOrDefault(ordinal, BLANK);
    }

}
