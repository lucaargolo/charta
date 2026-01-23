package dev.lucaargolo.charta.common.game.api.card;

import org.jetbrains.annotations.NotNull;

public record Rank(int ordinal) implements Comparable<Rank> {

    @Override
    public int compareTo(@NotNull Rank other) {
        return this.ordinal - other.ordinal;
    }

}
