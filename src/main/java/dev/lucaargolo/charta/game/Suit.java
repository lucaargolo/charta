package dev.lucaargolo.charta.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.lucaargolo.charta.Charta;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

public class Suit implements Comparable<Suit> {

    private static final LinkedHashSet<Suit> registry = new LinkedHashSet<>();
    private static final Int2ObjectMap<Suit> backing = new Int2ObjectArrayMap<>();

    private final ResourceLocation location;
    private final int id;

    public static final Codec<Suit> CODEC = ResourceLocation.CODEC.comapFlatMap(Suit::load, r -> r.location);

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

    public static final Suit BLANK = new Suit(Charta.id("blank"));

    public static final Suit SPADES = new Suit(Charta.id("spades"));
    public static final Suit HEARTS = new Suit(Charta.id("hearts"));
    public static final Suit CLUBS = new Suit(Charta.id("clubs"));
    public static final Suit DIAMONDS = new Suit(Charta.id("diamonds"));

    public static final Suit RED = new Suit(Charta.id("red"));
    public static final Suit YELLOW = new Suit(Charta.id("yellow"));
    public static final Suit GREEN = new Suit(Charta.id("green"));
    public static final Suit BLUE = new Suit(Charta.id("blue"));

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
