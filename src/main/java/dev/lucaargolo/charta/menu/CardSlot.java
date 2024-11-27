package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.utils.CardImage;

import java.util.List;
import java.util.function.Function;

public class CardSlot<G extends CardGame<G>> {

    public final G game;

    public int index = -1;
    public final float x;
    public final float y;
    private final Function<G, List<Card>> getter;
    private final Type type;

    public CardSlot(G game, Function<G, List<Card>> getter, float x, float y) {
        this(game, getter, x, y, Type.DEFAULT);
    }

    public CardSlot(G game, Function<G, List<Card>> getter, float x, float y, Type type) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.getter = getter;
        this.type = type;
    }

    public final List<Card> getCards() {
        return getter.apply(game);
    }

    public final void setCards(List<Card> cards) {
        List<Card> gameCards = getter.apply(game);
        gameCards.clear();
        gameCards.addAll(cards);
    }

    public final boolean insertCards(CardPlayer player, List<Card> collection, int index) {
        List<Card> cards = getter.apply(game);
        boolean result = index == -1 ? cards.addAll(collection) : cards.addAll(index % cards.size(), collection);
        for(Card card : collection) {
            onInsert(player, card);
        }
        return result;
    }

    public final Card removeCard(CardPlayer player, int index) {
        List<Card> cards = getter.apply(game);
        Card card = index == -1 ? cards.removeLast() : cards.remove(index % cards.size());
        onRemove(player, card);
        return card;
    }

    public void onInsert(CardPlayer player, Card card) {

    }

    public void onRemove(CardPlayer player, Card card) {

    }

    public boolean canInsertCard(CardPlayer player, List<Card> cards) {
        return true;
    }

    public boolean canRemoveCard(CardPlayer player) {
        List<Card> cards = getter.apply(game);
        return !cards.isEmpty();
    }

    public boolean isEmpty() {
        return getCards().isEmpty();
    }

    public Type getType() {
        return type;
    }

    public boolean isExtended() {
        return type == Type.INVENTORY || type == Type.PREVIEW;
    }

    public boolean isSmall() {
        return type == Type.SMALL || type == Type.PREVIEW;
    }

    public enum Type {
        DEFAULT,
        SMALL,
        INVENTORY,
        PREVIEW
    }

    public static float getWidth(CardSlot.Type type) {
        return switch (type) {
            case DEFAULT -> CardImage.WIDTH * 1.5f;
            case INVENTORY -> 150;
            case SMALL -> CardImage.WIDTH / 2f;
            case PREVIEW -> 41;
        };
    }

    public static float getWidth(CardSlot<?> slot) {
        return getWidth(slot.getType());
    }

    public static float getHeight(CardSlot.Type type) {
        return switch (type) {
            case DEFAULT, INVENTORY -> CardImage.HEIGHT * 1.5f;
            case SMALL, PREVIEW -> CardImage.HEIGHT / 2f;
        };
    }

    public static float getHeight(CardSlot<?> slot) {
        return getHeight(slot.getType());
    }

}

