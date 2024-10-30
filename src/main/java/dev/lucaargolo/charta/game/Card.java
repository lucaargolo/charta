package dev.lucaargolo.charta.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record Card(Suit suit, Rank rank) {

    public static final StreamCodec<ByteBuf, Card> STREAM_CODEC = StreamCodec.composite(
            Suit.STREAM_CODEC, Card::suit,
            Rank.STREAM_CODEC, Card::rank,
            Card::new
    );

    public static final StreamCodec<ByteBuf, List<Card>> LIST_STREAM_CODEC = ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC);

    public enum Suit {
        SPADES, HEARTS, CLUBS, DIAMONDS;

        public static final StreamCodec<ByteBuf, Suit> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Suit.values()[i], Suit::ordinal);
    }

    public enum Rank {
        BLANK, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, JOKER;

        public static final StreamCodec<ByteBuf, Rank> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Rank.values()[i], Rank::ordinal);
    }

}
