package dev.lucaargolo.charta.game;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Rank implements StringRepresentable {

    BLANK, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, JOKER;

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

}
