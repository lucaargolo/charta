package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;

import java.util.List;
import java.util.function.Function;

public class CardSlot<G extends CardGame> {

    public final G game;

    public int index = -1;
    public final int x;
    public final int y;
    private final Function<G, List<Card>> getter;
    private final boolean extended;

    public CardSlot(G game, Function<G, List<Card>> getter, int x, int y) {
        this(game, getter, x, y, false);
    }

    public CardSlot(G game, Function<G, List<Card>> getter, int x, int y, boolean extended) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.getter = getter;
        this.extended = extended;
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
        boolean result = index == -1 ? cards.addAll(collection) : cards.addAll(index, collection);
        for(Card card : collection) {
            onInsert(player, card);
        }
        return result;
    }

    public final Card removeCard(CardPlayer player, int index) {
        List<Card> cards = getter.apply(game);
        Card card = index == -1 ? cards.removeLast() : cards.remove(index);
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

    public boolean isExtended() {
        return extended;
    }

}

