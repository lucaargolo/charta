package dev.lucaargolo.charta.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Rank implements StringRepresentable {

    BLANK, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, JOKER;

    public static final StreamCodec<ByteBuf, Rank> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Rank.values()[i], Rank::ordinal);

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

}
