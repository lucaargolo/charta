package dev.lucaargolo.charta.common.game.api.card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.charta.common.data.ModDataComponentTypes;
import dev.lucaargolo.charta.common.game.Ranks;
import dev.lucaargolo.charta.common.game.Suits;
import net.minecraft.core.component.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class Card implements DataComponentHolder, Comparable<Card> {

    public static final Codec<Card> CODEC = Codec.lazyInitialized(() -> {
        return RecordCodecBuilder.create(instance -> instance.group(
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(card -> card.components.asPatch())
        ).apply(instance, Card::new));
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Card> STREAM_CODEC = StreamCodec.composite(
            DataComponentPatch.STREAM_CODEC,
            card -> card.components.asPatch(),
            Card::new
    );

    private final PatchedDataComponentMap components;

    private Card(PatchedDataComponentMap components) {
        this.components = components;
    }

    private Card(DataComponentPatch components) {
        this(PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components));
    }

    public Card() {
        this(new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return this.components;
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> component, @Nullable T value) {
        return this.components.set(component, value);
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> component, T defaultValue, U updateValue, BiFunction<T, U, T> updater) {
        return this.set(component, updater.apply(this.getOrDefault(component, defaultValue), updateValue));
    }

    @Nullable
    public <T> T update(DataComponentType<T> component, T defaultValue, UnaryOperator<T> updater) {
        T t = this.getOrDefault(component, defaultValue);
        return this.set(component, updater.apply(t));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> component) {
        return this.components.remove(component);
    }

    public void applyComponents(DataComponentPatch components) {
        this.components.applyPatch(components);
    }

    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    public Suit suit() {
        return components.getOrDefault(ModDataComponentTypes.SUIT.get(), Suits.BLANK);
    }

    public Rank rank() {
        return components.getOrDefault(ModDataComponentTypes.RANK.get(), Ranks.BLANK);
    }

    public boolean flipped() {
        return components.getOrDefault(ModDataComponentTypes.FLIPPED.get(), true);
    }

    public void flip() {
        this.update(ModDataComponentTypes.FLIPPED.get(), true, value -> !value);
    }

    public Card copy() {
        return new Card(this.components.copy());
    }

    public static Card create(Suit suit, Rank rank) {
        Card card = new Card();
        card.set(ModDataComponentTypes.SUIT.get(), suit);
        card.set(ModDataComponentTypes.RANK.get(), rank);
        card.set(ModDataComponentTypes.FLIPPED.get(), false);
        return card;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(this.components, card.components);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.components);
    }

    @Override
    public int compareTo(@NotNull Card other) {
        int suitComparison = this.suit().compareTo(other.suit());
        if (suitComparison != 0) return suitComparison;
        return this.rank().compareTo(other.rank());
    }
}
