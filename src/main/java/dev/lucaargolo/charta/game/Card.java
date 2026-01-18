package dev.lucaargolo.charta.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class Card implements Comparable<Card> {

    public static final Codec<Card> CODEC = Codec.STRING.comapFlatMap(
        card -> {
            String[] elements = card.split("_");
            try {
                ResourceLocation suit = ResourceLocation.parse(elements[0]);
                ResourceLocation rank;
                if(elements[1].contains(":")) {
                    rank = ResourceLocation.parse(elements[1]);
                }else{
                    rank = ResourceLocation.fromNamespaceAndPath(suit.getNamespace(), elements[1]);
                }
                return DataResult.success(new Card(Suit.load(suit).getOrThrow(), Rank.load(rank).getOrThrow()));
            }catch (Exception e) {
                return DataResult.error(() -> "Invalid card format: " + card);
            }
        }, Card::toString
    );

    public static final StreamCodec<ByteBuf, Card> STREAM_CODEC = StreamCodec.composite(
            Suit.STREAM_CODEC, Card::suit,
            Rank.STREAM_CODEC, Card::rank,
            ByteBufCodecs.BOOL, Card::flipped,
            Card::new
    );

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

    public Suit suit() {
        return suit;
    }

    public Rank rank() {
        return rank;
    }

    public boolean flipped() {
        return flipped;
    }

    public void flip() {
        this.flipped = !this.flipped;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return flipped == card.flipped && suit.equals(card.suit) && rank.equals(card.rank);
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

    @Override
    public String toString() {
        if(suit.location().getNamespace().equals(rank.location().getNamespace())) {
            String namespace = suit.location().getNamespace();
            return namespace + ":" + suit.location().getPath() + "_" + rank.location().getPath();
        }else{
            return suit + "_" + rank;
        }
    }

    public Card copy() {
        return new Card(suit, rank, flipped);
    }

}
