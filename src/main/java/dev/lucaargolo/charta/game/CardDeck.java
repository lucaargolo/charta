package dev.lucaargolo.charta.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.ExpandedStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CardDeck {

    public static final StreamCodec<ByteBuf, CardDeck> STREAM_CODEC = ExpandedStreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC),
        CardDeck::getCards,
        ByteBufCodecs.map(HashMap::new, Suit.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getSuitsLocation,
        ByteBufCodecs.map(HashMap::new, Suit.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
        CardDeck::getSuitsTranslatableKeys,
        ByteBufCodecs.map(HashMap::new, Card.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getCardsLocation,
        ByteBufCodecs.map(HashMap::new, Card.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
        CardDeck::getCardsTranslatableKeys,
        ResourceLocation.STREAM_CODEC,
        CardDeck::getDeckLocation,
        ByteBufCodecs.STRING_UTF8,
        CardDeck::getDeckTranslatableKey,
        CardDeck::new
    );

    public static final Codec<CardDeck> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Card.CODEC.listOf().fieldOf("cards").forGetter(CardDeck::getCards),
            Codec.simpleMap(Suit.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Suit.values())).fieldOf("suits_images").forGetter(CardDeck::getSuitsLocation),
            Codec.simpleMap(Suit.CODEC, Codec.STRING, StringRepresentable.keys(Suit.values())).fieldOf("suits_keys").forGetter(CardDeck::getSuitsTranslatableKeys),
            Codec.simpleMap(Card.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Card.values())).fieldOf("cards_images").forGetter(CardDeck::getCardsLocation),
            Codec.simpleMap(Card.CODEC, Codec.STRING, StringRepresentable.keys(Suit.values())).fieldOf("cards_keys").forGetter(CardDeck::getCardsTranslatableKeys),
            ResourceLocation.CODEC.fieldOf("deck_image").forGetter(CardDeck::getDeckLocation),
            Codec.STRING.fieldOf("deck_key").forGetter(CardDeck::getDeckTranslatableKey)
        ).apply(instance, CardDeck::new);
    });

    private final ImmutableList<Card> cards;

    private final Function<Suit, ResourceLocation> suitsLocation;
    private final Function<Suit, String> suitsTranslatableKeys;

    private final Function<Card, ResourceLocation> cardsLocation;
    private final Function<Card, String> cardsTranslatableKeys;

    private final Supplier<ResourceLocation> deckLocation;
    private final Supplier<String> deckTranslatableKey;

    private CardDeck(List<Card> cards, Map<Suit, ResourceLocation> suitsLocation, Map<Suit, String> suitsTranslatableKey, Map<Card, ResourceLocation> cardsLocation, Map<Card, String> cardsTranslatableKey, ResourceLocation deckLocation, String deckTranslatableKey) {
        this(cards, suit -> suitsLocation.getOrDefault(suit, Charta.MISSING_SUIT), suit -> suitsTranslatableKey.getOrDefault(suit, "charta.suit.unknown"), card -> cardsLocation.getOrDefault(card, Charta.MISSING_CARD), card -> cardsTranslatableKey.getOrDefault(card, "charta.card.unknown"), () -> deckLocation, () -> deckTranslatableKey);
    }

    public CardDeck(List<Card> cards, Function<Suit, ResourceLocation> suitsLocation, Function<Suit, String> suitsTranslatableKey, Function<Card, ResourceLocation> cardsLocation, Function<Card, String> cardsTranslatableKey, Supplier<ResourceLocation> deckLocation, Supplier<String> deckTranslatableKey) {
        this.cards = ImmutableList.copyOf(cards);
        this.suitsLocation = suitsLocation;
        this.suitsTranslatableKeys = suitsTranslatableKey;
        this.cardsLocation = cardsLocation;
        this.cardsTranslatableKeys = cardsTranslatableKey;
        this.deckLocation = deckLocation;
        this.deckTranslatableKey = deckTranslatableKey;
    }

    public Component getName() {
        return Component.translatable(deckTranslatableKey.get());
    }

    public ResourceLocation getSuitTexture(Suit suit) {
        return ChartaClient.getSuitTexture(suitsLocation.apply(suit));
    }

    public String getSuitTranslatableKey(Suit suit) {
        return suitsTranslatableKeys.apply(suit);
    }

    public ResourceLocation getCardTexture(Card card) {
        return ChartaClient.getCardTexture(cardsLocation.apply(card));
    }

    public String getCardTranslatableKey(Card card) {
        return cardsTranslatableKeys.apply(card);
    }

    public ResourceLocation getDeckTexture() {
        return ChartaClient.getDeckTexture(deckLocation.get());
    }

    //CODEC Getters

    public ImmutableList<Card> getCards() {
        return cards;
    }

    private Map<Suit, ResourceLocation> getSuitsLocation() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Suit.values())), suitsLocation::apply);
    }

    private Map<Suit, String> getSuitsTranslatableKeys() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Suit.values())), suitsTranslatableKeys::apply);
    }

    private Map<Card, ResourceLocation> getCardsLocation() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Card.values())), cardsLocation::apply);
    }

    private Map<Card, String> getCardsTranslatableKeys() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Card.values())), cardsTranslatableKeys::apply);
    }

    public ResourceLocation getDeckLocation() {
        return deckLocation.get();
    }

    public String getDeckTranslatableKey() {
        return deckTranslatableKey.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDeck cardDeck = (CardDeck) o;

        if (!Objects.equals(deckTranslatableKey, cardDeck.deckTranslatableKey)) return false;
        if (!cards.equals(cardDeck.cards)) return false;

        for (Card card : cards) {
            ResourceLocation thisLocation = cardsLocation.apply(card);
            ResourceLocation otherLocation = cardDeck.cardsLocation.apply(card);
            if (!Objects.equals(thisLocation, otherLocation)) return false;
        }

        return Objects.equals(deckLocation.get(), cardDeck.deckLocation.get());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(deckTranslatableKey, cards, deckLocation.get());

        for (Card card : cards) {
            result = 31 * result + Objects.hashCode(cardsLocation.apply(card));
        }

        return result;
    }

    public static CardDeck simple(ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if(suit != Suit.BLANK) {
                for (Rank rank : Rank.values()) {
                    if (rank != Rank.BLANK && rank != Rank.JOKER) {
                        deck.add(new Card(suit, rank));
                    }
                }
            }
        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath();
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey +=  "_" + deckLocation.getPath();
        }
        String deckTranslatableKey = translatableKey;
        return new CardDeck(deck, (suit) -> {
            return cardLocation.withSuffix("/" + suit.ordinal());
        }, (suit) -> {
            return "suit.charta."+(suit == Suit.BLANK ? "unknown" : suit.getSerializedName());
        }, (card) -> {
            return cardLocation.withSuffix( "/" + card.getSuit().ordinal() + "_" + card.getRank().ordinal());
        }, (card) -> {
            return card.getRank() == Rank.BLANK ? "suit.charta."+(card.getSuit() == Suit.BLANK ? "unknown" : card.getSuit().getSerializedName()) : "card.charta."+(card.getSuit() == Suit.BLANK ? "unknown" : card.getRank().getSerializedName()+"."+card.getSuit().getSerializedName());
        }, () -> deckLocation, () -> deckTranslatableKey);
    }

    public static CardDeck fun(ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if(suit != Suit.BLANK) {
                for (Rank rank : Rank.values()) {
                    deck.add(new Card(suit, rank));
                    if(rank != Rank.BLANK && rank != Rank.JOKER && rank != Rank.TEN) {
                        deck.add(new Card(suit, rank));
                    }
                }
            }
        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath();
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey +=  "_" + deckLocation.getPath();
        }
        String deckTranslatableKey = translatableKey;
        return new CardDeck(deck, (suit) -> {
            return cardLocation.withSuffix("/" + suit.ordinal());
        }, (suit) -> {
            return "suit.charta."+getFunSuit(suit);
        }, (card) -> {
            return cardLocation.withSuffix( "/" + card.getSuit().ordinal() + "_" + card.getRank().ordinal());
        }, (card) -> {
            return "card.charta."+getFunCardKey(card);
        }, () -> deckLocation, () -> deckTranslatableKey);
    }

    private static String getFunSuit(Suit suit) {
        return switch (suit) {
            case SPADES -> "red";
            case HEARTS -> "yellow";
            case CLUBS -> "green";
            case DIAMONDS -> "blue";
            default -> "unknown";
        };
    }

    private static String getFunCardKey(Card card) {
        if(card.getSuit() == Suit.BLANK) {
            return "unknown";
        }
        String rank = switch (card.getRank()) {
            case BLANK -> "wild";
            case TEN -> "zero";
            case JACK -> "block";
            case QUEEN -> "reverse";
            case KING -> "plus_two";
            case JOKER -> "wild_plus_four";
            default -> card.getRank().getSerializedName();
        };
        return switch (card.getRank()) {
            case BLANK, JOKER -> rank;
            default -> getFunSuit(card.getSuit())+"."+rank;
        };
    }

}
