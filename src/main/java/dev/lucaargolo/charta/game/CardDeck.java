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
import net.minecraft.world.item.Rarity;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CardDeck {

    public static final StreamCodec<ByteBuf, CardDeck> STREAM_CODEC = ExpandedStreamCodec.composite(
        Rarity.STREAM_CODEC,
        CardDeck::getRarity,
        ByteBufCodecs.BOOL,
        CardDeck::isTradeable,
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
            Rarity.CODEC.fieldOf("rarity").forGetter(CardDeck::getRarity),
            Codec.BOOL.fieldOf("tradeable").forGetter(CardDeck::isTradeable),
            Card.CODEC.listOf().fieldOf("cards").forGetter(CardDeck::getCards),
            Codec.simpleMap(Suit.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Suit.values())).fieldOf("suits_images").forGetter(CardDeck::getSuitsLocation),
            Codec.simpleMap(Suit.CODEC, Codec.STRING, StringRepresentable.keys(Suit.values())).fieldOf("suits_keys").forGetter(CardDeck::getSuitsTranslatableKeys),
            Codec.simpleMap(Card.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Card.values())).fieldOf("cards_images").forGetter(CardDeck::getCardsLocation),
            Codec.simpleMap(Card.CODEC, Codec.STRING, StringRepresentable.keys(Suit.values())).fieldOf("cards_keys").forGetter(CardDeck::getCardsTranslatableKeys),
            ResourceLocation.CODEC.fieldOf("deck_image").forGetter(CardDeck::getDeckLocation),
            Codec.STRING.fieldOf("deck_key").forGetter(CardDeck::getDeckTranslatableKey)
        ).apply(instance, CardDeck::new);
    });

    private final Rarity rarity;
    private final boolean tradeable;
    private final ImmutableList<Card> cards;

    private final Function<Suit, ResourceLocation> suitsLocation;
    private final Function<Suit, String> suitsTranslatableKeys;

    private final Function<Card, ResourceLocation> cardsLocation;
    private final Function<Card, String> cardsTranslatableKeys;

    private final Supplier<ResourceLocation> deckLocation;
    private final Supplier<String> deckTranslatableKey;

    private CardDeck(Rarity rarity, boolean tradeable, List<Card> cards, Map<Suit, ResourceLocation> suitsLocation, Map<Suit, String> suitsTranslatableKey, Map<Card, ResourceLocation> cardsLocation, Map<Card, String> cardsTranslatableKey, ResourceLocation deckLocation, String deckTranslatableKey) {
        this(rarity, tradeable, cards, suit -> suitsLocation.getOrDefault(suit, Charta.MISSING_SUIT), suit -> suitsTranslatableKey.getOrDefault(suit, "charta.suit.unknown"), card -> cardsLocation.getOrDefault(card, Charta.MISSING_CARD), card -> cardsTranslatableKey.getOrDefault(card, "charta.card.unknown"), () -> deckLocation, () -> deckTranslatableKey);
    }

    public CardDeck(Rarity rarity, boolean tradeable, List<Card> cards, Function<Suit, ResourceLocation> suitsLocation, Function<Suit, String> suitsTranslatableKey, Function<Card, ResourceLocation> cardsLocation, Function<Card, String> cardsTranslatableKey, Supplier<ResourceLocation> deckLocation, Supplier<String> deckTranslatableKey) {
        this.rarity = rarity;
        this.tradeable = tradeable;
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

    public Rarity getRarity() {
        return rarity;
    }

    public boolean isTradeable() {
        return tradeable;
    }

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
        CardDeck deck = (CardDeck) o;
        return rarity == deck.rarity &&
            Objects.equals(cards, deck.cards) &&
            Objects.equals(getSuitsLocation(), deck.getSuitsLocation()) &&
            Objects.equals(getSuitsTranslatableKeys(), deck.getSuitsTranslatableKeys()) &&
            Objects.equals(getCardsLocation(), deck.getCardsLocation()) &&
            Objects.equals(getCardsTranslatableKeys(), deck.getCardsTranslatableKeys()) &&
            Objects.equals(deckLocation.get(), deck.deckLocation.get()) &&
            Objects.equals(deckTranslatableKey.get(), deck.deckTranslatableKey.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            rarity,
            cards,
            getSuitsLocation(),
            getSuitsTranslatableKeys(),
            getCardsLocation(),
            getCardsTranslatableKeys(),
            deckLocation.get(),
            deckTranslatableKey.get()
        );
    }

    public static CardDeck simple(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
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
        return new CardDeck(rarity, canBeTraded, deck, (suit) -> {
            return cardLocation.withSuffix("/" + suit.ordinal());
        }, (suit) -> {
            return "suit.charta."+(suit == Suit.BLANK ? "unknown" : suit.getSerializedName());
        }, (card) -> {
            return cardLocation.withSuffix( "/" + card.getSuit().ordinal() + "_" + card.getRank().ordinal());
        }, (card) -> {
            return card.getRank() == Rank.BLANK ? "suit.charta."+(card.getSuit() == Suit.BLANK ? "unknown" : card.getSuit().getSerializedName()) : "card.charta."+(card.getSuit() == Suit.BLANK ? "unknown" : card.getSuit().getSerializedName()+"."+card.getRank().getSerializedName());
        }, () -> deckLocation, () -> deckTranslatableKey);
    }

    public static CardDeck fun(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
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
        return new CardDeck(rarity, canBeTraded, deck, (suit) -> {
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
            case ACE -> "one";
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
