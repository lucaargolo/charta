package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.Charta;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Card implements Comparable<Card> {

    public static final StreamCodec<ByteBuf, Card> STREAM_CODEC = StreamCodec.composite(
            Suit.STREAM_CODEC, Card::getSuit,
            Rank.STREAM_CODEC, Card::getRank,
            ByteBufCodecs.BOOL, Card::isFlipped,
            Card::new
    );

    public static final StreamCodec<ByteBuf, List<Card>> LIST_STREAM_CODEC = ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC);

    public static final Card BLANK = new Card(Suit.BLANK, Rank.BLANK, true);

    private final Suit suit;
    private final Rank rank;
    private boolean flipped;

    public Card(Suit suit, Rank rank) {
        this(suit, rank, false);
    }

    public Card(Suit suit, Rank rank, boolean flipped) {
        this.suit = suit;
        this.rank = rank;
        this.flipped = flipped;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void flip() {
        this.flipped = !this.flipped;
    }

    //TODO: Remove this
    public ResourceLocation getId() {
        return flipped ? Charta.id("red") : Charta.id("standard/"+suit.ordinal()+"_"+rank.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return flipped == card.flipped && suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank, flipped);
    }

    @Override
    public int compareTo(Card other) {
        int suitComparison = this.suit.compareTo(other.suit);
        if (suitComparison != 0) return suitComparison;
        return this.rank.compareTo(other.rank);
    }

    public enum Suit {
        BLANK, SPADES, HEARTS, CLUBS, DIAMONDS;

        public static final StreamCodec<ByteBuf, Suit> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Suit.values()[i], Suit::ordinal);
    }

    public enum Rank {
        BLANK, ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, JOKER;

        public static final StreamCodec<ByteBuf, Rank> STREAM_CODEC = ByteBufCodecs.idMapper(i -> Rank.values()[i], Rank::ordinal);
    }

}
