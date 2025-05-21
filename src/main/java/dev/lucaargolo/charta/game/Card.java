package dev.lucaargolo.charta.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Card implements Comparable<Card>, StringRepresentable {

    private static final Card[] CARDS = new Card[Suit.values().length * Rank.values().length];

    static {
        int i = 0;
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                CARDS[i++] = new Card(suit, rank);
            }
        }
    }

    public static final Codec<Card> CODEC = Codec.STRING.comapFlatMap(Card::read, Card::toString).stable();

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

    @Override
    public String toString() {
        return suit.getSerializedName()+":"+rank.getSerializedName();
    }

    public static DataResult<Card> read(String string) {
        try {
            String[] in = string.split(":");
            Card card = new Card(Suit.valueOf(in[0].toUpperCase()), Rank.valueOf(in[1].toUpperCase()));
            return DataResult.success(card);
        } catch (Exception e) {
            return DataResult.error(() -> {
                return "Not a valid card: " + string + " " + e.getMessage();
            });
        }
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.toString();
    }

    public static Card[] values() {
        return CARDS;
    }

    public Card copy() {
        return new Card(suit, rank, flipped);
    }

    public void toBuf(FriendlyByteBuf buf) {
        buf.writeEnum(suit);
        buf.writeEnum(rank);
        buf.writeBoolean(flipped);
    }

    public static Card fromBuf(FriendlyByteBuf buf) {
        Suit suit = buf.readEnum(Suit.class);
        Rank rank = buf.readEnum(Rank.class);
        boolean flipped = buf.readBoolean();
        return new Card(suit, rank, flipped);
    }

}
