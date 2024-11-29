package dev.lucaargolo.charta.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Suit implements StringRepresentable {

    BLANK, SPADES, HEARTS, CLUBS, DIAMONDS;

    public static final StreamCodec<ByteBuf, Suit> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Suit.values()[i], Suit::ordinal);
    public static final Codec<Suit> CODEC = Codec.STRING.comapFlatMap(Suit::fromSerializedName, Suit::getSerializedName).stable();

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

    public static DataResult<Suit> fromSerializedName(String name) {
        try {
            return DataResult.success(Suit.valueOf(name.toUpperCase()));
        } catch (Exception e) {
            return DataResult.error(() -> {
                return "Not a valid suit: " + name + " " + e.getMessage();
            });
        }
    }

}
