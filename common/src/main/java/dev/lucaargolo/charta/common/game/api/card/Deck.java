package dev.lucaargolo.charta.common.game.api.card;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.client.compat.IrisCompat;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.Ranks;
import dev.lucaargolo.charta.common.game.Suits;
import dev.lucaargolo.charta.common.utils.CardImageUtils;
import dev.lucaargolo.charta.common.utils.SuitImage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Deck {

    public static final Deck EMPTY = new Deck(Rarity.COMMON, false, List.of(), s -> ChartaMod.MISSING_SUIT, s -> ChartaMod.MISSING_SUIT_TRANSLATION, c -> ChartaMod.MISSING_CARD, c -> ChartaMod.MISSING_CARD_TRANSLATION, ChartaMod.MISSING_DECK, ChartaMod.MISSING_DECK_TRANSLATION);

    public static final StreamCodec<RegistryFriendlyByteBuf, Deck> STREAM_CODEC = StreamCodec.composite(
        Rarity.STREAM_CODEC,
        deck -> deck.rarity,
        ByteBufCodecs.BOOL,
        deck -> deck.tradeable,
        ResourceLocation.STREAM_CODEC,
        deck -> deck.texture,
        ByteBufCodecs.STRING_UTF8,
        deck -> deck.translation,
        ByteBufCodecs.collection(ArrayList::new, SuitDefinition.STREAM_CODEC),
        deck -> deck.suits.stream().toList(),
        ByteBufCodecs.collection(ArrayList::new, CardDefinition.STREAM_CODEC),
        deck -> deck.cards,
        Deck::new
    );

    public static final Codec<Deck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Rarity.CODEC.fieldOf("rarity").forGetter(deck -> deck.rarity),
        Codec.BOOL.fieldOf("tradeable").forGetter(deck -> deck.tradeable),
        ResourceLocation.CODEC.fieldOf("texture").forGetter(deck -> deck.texture),
        Codec.STRING.fieldOf("translation").forGetter(deck -> deck.translation),
        SuitDefinition.CODEC.listOf().fieldOf("suits").forGetter(deck -> deck.suits.stream().toList()),
        CardDefinition.CODEC.listOf().fieldOf("cards").forGetter(deck -> deck.cards)
    ).apply(instance, Deck::new));

    private final Rarity rarity;
    private final boolean tradeable;
    private final ResourceLocation texture;
    private final String translation;

    private final ImmutableSortedSet<SuitDefinition> suits;
    private final ImmutableList<CardDefinition> cards;

    public Deck(Rarity rarity, boolean tradeable, List<Card> cards, Function<Suit, ResourceLocation> suitTextures, Function<Suit, String> suitTranslations, Function<Card, ResourceLocation> cardTextures, Function<Card, String> cardTranslations, ResourceLocation texture, String translation) {
        this(rarity, tradeable, texture, translation, createDefinitions(cards, suitTextures, suitTranslations, cardTextures, cardTranslations));
    }

    public Deck(Rarity rarity, boolean tradeable, ResourceLocation texture, String translation, List<SuitDefinition> suits, List<CardDefinition> cards) {
        this(rarity, tradeable, texture, translation, ImmutableSortedSet.copyOf(suits), ImmutableList.sortedCopyOf(cards));
    }

    private Deck(Rarity rarity, boolean tradeable, ResourceLocation texture, String translation, Pair<ImmutableSortedSet<SuitDefinition>, ImmutableList<CardDefinition>> definitions) {
        this(rarity, tradeable, texture, translation, definitions.getFirst(), definitions.getSecond());
    }

    private Deck(Rarity rarity, boolean tradeable, ResourceLocation texture, String translation, ImmutableSortedSet<SuitDefinition> suits, ImmutableList<CardDefinition> cards) {
        this.rarity = rarity;
        this.tradeable = tradeable;
        this.texture = texture;
        this.translation = translation;
        this.suits = suits;
        this.cards = cards;
    }

    public boolean isTradeable() {
        return tradeable;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public ResourceLocation getTexture(boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getDeckGlowTexture(texture);
        }else{
            return ChartaModClient.getDeckTexture(texture);
        }
    }

    public Component getName() {
        return Component.translatable(translation);
    }

    public String getTranslation() {
        return translation;
    }

    public List<Card> getCards() {
        return cards.stream().map(CardDefinition::card).toList();
    }

    public List<Suit> getSuits() {
        return suits.stream().map(SuitDefinition::suit).toList();
    }

    public ResourceLocation getSuitTexture(Suit suit, boolean glow) {
        ResourceLocation texture = this.suits.stream().filter(s ->  s.suit.equals(suit)).findFirst().map(SuitDefinition::texture).orElse(ChartaMod.MISSING_SUIT);
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getSuitGlowTexture(texture);
        }else {
            return ChartaModClient.getSuitTexture(texture);
        }
    }

    public String getSuitTranslatableKey(Suit suit) {
        return this.suits.stream().filter(s -> s.suit.equals(suit)).findFirst().map(SuitDefinition::translation).orElse(ChartaMod.MISSING_SUIT_TRANSLATION);
    }

    public ResourceLocation getCardTexture(Card card, boolean glow) {
        ResourceLocation texture = this.cards.stream().filter(c -> c.card.equals(card)).findFirst().map(CardDefinition::texture).orElse(ChartaMod.MISSING_CARD);
        if(glow && IrisCompat.isPresent()) {
            return IrisCompat.getCardGlowTexture(texture);
        }else {
            return ChartaModClient.getCardTexture(texture);
        }
    }

    public String getCardTranslatableKey(Card card) {
        return this.cards.stream().filter(c -> c.card.equals(card)).findFirst().map(CardDefinition::translation).orElse(ChartaMod.MISSING_CARD_TRANSLATION);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Deck deck = (Deck) o;
        return tradeable == deck.tradeable && rarity == deck.rarity && Objects.equals(suits, deck.suits) && Objects.equals(cards, deck.cards) && Objects.equals(texture, deck.texture) && Objects.equals(translation, deck.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rarity, cards, suits, texture, translation);
    }

    public static Deck simple(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        return simple(rarity, canBeTraded, cardLocation, cardLocation, deckLocation);
    }

    public static Deck simple(Rarity rarity, boolean canBeTraded, ResourceLocation suitLocation, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suits.STANDARD) {
            for (Rank rank : Ranks.STANDARD) {
                deck.add(Card.create(suit, rank));
            }
        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath().replace("/", ".");
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey =  "deck." + deckLocation.getNamespace() + "." + deckLocation.getPath().replace("/", ".");
        }
        String deckTranslatableKey = translatableKey;
        return new Deck(rarity, canBeTraded, deck, (suit) -> suitLocation.withSuffix("/" + Suits.getLocation(suit).getPath()), (suit) -> "suit.charta."+Suits.getLocation(suit).getPath(), (card) -> cardLocation.withSuffix( "/" + Suits.getLocation(card.suit()).getPath() + "_" + Ranks.getLocation(card.rank()).getPath()), (card) -> "card.charta."+Suits.getLocation(card.suit()).getPath()+"."+Ranks.getLocation(card.rank()).getPath(), deckLocation, deckTranslatableKey);
    }

    public static Deck fun(Rarity rarity, boolean canBeTraded, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        return fun(rarity, canBeTraded, cardLocation, cardLocation, deckLocation);
    }

    public static Deck fun(Rarity rarity, boolean canBeTraded, ResourceLocation suitLocation, ResourceLocation cardLocation, ResourceLocation deckLocation) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suits.FUN) {
            for (Rank rank : Ranks.FUN) {
                deck.add(Card.create(suit, rank));
                if(rank != Ranks.WILD && rank != Ranks.WILD_PLUS_4 && rank != Ranks.ZERO) {
                    deck.add(Card.create(suit, rank));
                }
            }

        }
        String translatableKey = "deck." + deckLocation.getNamespace() + "." + cardLocation.getPath().replace("/", ".");
        if(!cardLocation.getPath().equals(deckLocation.getPath())) {
            translatableKey =  "deck." + deckLocation.getNamespace() + "." + deckLocation.getPath().replace("/", ".");
        }
        String deckTranslatableKey = translatableKey;
        return new Deck(rarity, canBeTraded, deck, (suit) -> suitLocation.withSuffix("/" + Suits.getLocation(suit).getPath()), (suit) -> "suit.charta."+Suits.getLocation(suit).getPath(), (card) -> cardLocation.withSuffix( "/" + Suits.getLocation(card.suit()).getPath() + "_" + Ranks.getLocation(card.rank()).getPath()), (card) -> card.rank() == Ranks.WILD || card.rank() == Ranks.WILD_PLUS_4 ? "card.charta."+Ranks.getLocation(card.rank()).getPath() : "card.charta."+Suits.getLocation(card.suit()).getPath()+"."+Ranks.getLocation(card.rank()).getPath(), deckLocation, deckTranslatableKey);
    }

    public int getCardColor(Card card) {
        return getSuitColor(card.suit());
    }

    public int getSuitColor(Suit suit) {
        ResourceLocation texture = this.suits.stream().filter(s ->  s.suit.equals(suit)).findFirst().map(SuitDefinition::texture).orElse(ChartaMod.MISSING_SUIT);
        SuitImage image = ChartaMod.SUIT_IMAGES.getImages().getOrDefault(texture, CardImageUtils.EMPTY_SUIT);
        if(image == CardImageUtils.EMPTY_SUIT) {
            return 0xFFFFFF;
        }
        int color = image.getAverageColor();
        Vec3 col = Vec3.fromRGB24(color);
        double brightness = 0.299 * col.x + 0.587 * col.y + 0.114 * col.z;
        if (brightness < 0.5) {
            double factor = 2.5 * 255;
            int r = Math.min(255, (int)(col.x * factor));
            int g = Math.min(255, (int)(col.y * factor));
            int b = Math.min(255, (int)(col.z * factor));

            return (r << 16) | (g << 8) | b;
        }else{
            return color;
        }
    }

    private static Pair<ImmutableSortedSet<SuitDefinition>, ImmutableList<CardDefinition>> createDefinitions(List<Card> cards, Function<Suit, ResourceLocation> suitTextures, Function<Suit, String> suitTranslations, Function<Card, ResourceLocation> cardTextures, Function<Card, String> cardTranslations) {
        ImmutableSortedSet.Builder<SuitDefinition> suitBuilder = ImmutableSortedSet.naturalOrder();
        ImmutableList.Builder<CardDefinition> cardBuilder = ImmutableList.builder();
        for(Card card : cards) {
            Suit suit = card.suit();
            suitBuilder.add(new SuitDefinition(suit, suitTextures.apply(suit), suitTranslations.apply(suit)));
            cardBuilder.add(new CardDefinition(card, cardTextures.apply(card), cardTranslations.apply(card)));
        }
        return Pair.of(suitBuilder.build(), cardBuilder.build());
    }

    public record SuitDefinition(Suit suit, ResourceLocation texture, String translation) implements Comparable<SuitDefinition> {

        public static Codec<SuitDefinition> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Suits.getCodec().fieldOf("suit").forGetter(SuitDefinition::suit),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(SuitDefinition::texture),
                    Codec.STRING.fieldOf("translation").forGetter(SuitDefinition::translation)
            ).apply(instance, SuitDefinition::new);
        });

        public static StreamCodec<RegistryFriendlyByteBuf, SuitDefinition> STREAM_CODEC = StreamCodec.composite(
                Suits.getStreamCodec(),
                SuitDefinition::suit,
                ResourceLocation.STREAM_CODEC,
                SuitDefinition::texture,
                ByteBufCodecs.STRING_UTF8,
                SuitDefinition::translation,
                SuitDefinition::new
        );

        @Override
        public int compareTo(@NotNull Deck.SuitDefinition o) {
            return suit.compareTo(o.suit);
        }
    }

    public record CardDefinition(Card card, ResourceLocation texture, String translation) implements Comparable<CardDefinition> {

        public static Codec<CardDefinition> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Card.CODEC.fieldOf("card").forGetter(CardDefinition::card),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(CardDefinition::texture),
                    Codec.STRING.fieldOf("translation").forGetter(CardDefinition::translation)
            ).apply(instance, CardDefinition::new);
        });

        public static StreamCodec<RegistryFriendlyByteBuf, CardDefinition> STREAM_CODEC = StreamCodec.composite(
                Card.STREAM_CODEC,
                CardDefinition::card,
                ResourceLocation.STREAM_CODEC,
                CardDefinition::texture,
                ByteBufCodecs.STRING_UTF8,
                CardDefinition::translation,
                CardDefinition::new
        );

        @Override
        public int compareTo(@NotNull Deck.CardDefinition o) {
            return card.compareTo(o.card);
        }
    }

}
