package dev.lucaargolo.charta.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CardDeck {

    public static final StreamCodec<ByteBuf, CardDeck> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        CardDeck::getTranslatableKey,
        ByteBufCodecs.map(HashMap::new, Suit.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getSuitsLocation,
        ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC),
        CardDeck::getCards,
        ByteBufCodecs.map(HashMap::new, Card.STREAM_CODEC, ResourceLocation.STREAM_CODEC),
        CardDeck::getCardsLocation,
        ResourceLocation.STREAM_CODEC,
        CardDeck::getDeckLocation,
        CardDeck::new
    );

    public static final Codec<CardDeck> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.STRING.fieldOf("key").forGetter(CardDeck::getTranslatableKey),
            Codec.simpleMap(Suit.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Suit.values())).fieldOf("suits").forGetter(CardDeck::getSuitsLocation),
            Card.CODEC.listOf().fieldOf("deck").forGetter(CardDeck::getCards),
            Codec.simpleMap(Card.CODEC, ResourceLocation.CODEC, StringRepresentable.keys(Card.values())).fieldOf("images").forGetter(CardDeck::getCardsLocation),
            ResourceLocation.CODEC.fieldOf("image").forGetter(CardDeck::getDeckLocation)
        ).apply(instance, CardDeck::new);
    });

    private final String translatableKey;
    private final Function<Suit, ResourceLocation> suitsLocation;
    private final ImmutableList<Card> cards;
    private final Function<Card, ResourceLocation> cardsLocation;
    private final Supplier<ResourceLocation> deckLocation;

    private CardDeck(String translatableKey, Map<Suit, ResourceLocation> suitsLocation, List<Card> cards, Map<Card, ResourceLocation> cardsLocation, ResourceLocation deckLocation) {
        this(translatableKey, suit -> suitsLocation.getOrDefault(suit, Charta.MISSING_SUIT), cards, card -> cardsLocation.getOrDefault(card, Charta.MISSING_CARD), () -> deckLocation);
    }

    public CardDeck(String translatableKey, Function<Suit, ResourceLocation> suitsLocation, List<Card> cards, Function<Card, ResourceLocation> cardsLocation, Supplier<ResourceLocation> deckLocation) {
        this.translatableKey = translatableKey;
        this.suitsLocation = suitsLocation;
        this.cards = ImmutableList.copyOf(cards);
        this.cardsLocation = cardsLocation;
        this.deckLocation = deckLocation;
    }

    public String getTranslatableKey() {
        return translatableKey;
    }

    public Component getName() {
        return Component.translatable(this.translatableKey);
    }

    public ResourceLocation getSuitTexture(Suit suit) {
        return ChartaClient.getSuitTexture(suitsLocation.apply(suit));
    }

    private Map<Suit, ResourceLocation> getSuitsLocation() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Suit.values())), suitsLocation::apply);
    }

    public ImmutableList<Card> getCards() {
        return cards;
    }

    public ResourceLocation getCardTexture(Card card) {
        return ChartaClient.getCardTexture(cardsLocation.apply(card));
    }

    public ResourceLocation getDeckLocation() {
        return deckLocation.get();
    }

    public ResourceLocation getDeckTexture() {
        return ChartaClient.getDeckTexture(deckLocation.get());
    }

    private Map<Card, ResourceLocation> getCardsLocation() {
        return Maps.asMap(new TreeSet<>(Arrays.asList(Card.values())), cardsLocation::apply);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDeck cardDeck = (CardDeck) o;

        if (!Objects.equals(translatableKey, cardDeck.translatableKey)) return false;
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
        int result = Objects.hash(translatableKey, cards, deckLocation.get());

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
        return new CardDeck(translatableKey, (suit) -> {
            return cardLocation.withSuffix("/" + suit.ordinal());
        }, deck, (card) -> {
            return cardLocation.withSuffix( "/" + card.getSuit().ordinal() + "_" + card.getRank().ordinal());
        }, () -> deckLocation);
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
        return new CardDeck(translatableKey, (suit) -> {
            return cardLocation.withSuffix("/" + suit.ordinal());
        }, deck, (card) -> {
            return cardLocation.withSuffix("/" + card.getSuit().ordinal() + "_" + card.getRank().ordinal());
        }, () -> deckLocation);
    }

}
