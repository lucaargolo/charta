package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.utils.CardImage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CardSlot<G extends CardGame<G>> {

    public int index = -1;

    protected final G game;
    protected final Function<G, GameSlot> getter;

    public final float x;
    public final float y;

    private final Type type;

    public CardSlot(G game, Function<G, GameSlot> getter, float x, float y) {
        this(game, getter, x, y, Type.DEFAULT);
    }

    public CardSlot(G game, Function<G, GameSlot> getter, float x, float y, Type type) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.getter = getter;
        this.type = type;
    }

    public final GameSlot getSlot() {
        return getter.apply(game);
    }

    public final void setCards(List<Card> cards) {
        GameSlot slot = getter.apply(game);
        slot.clear();
        slot.addAll(cards);
    }

    public final boolean insertCards(GameSlot collection, int index) {
        GameSlot slot = getter.apply(game);
        return index == -1 ? slot.addAll(collection) : slot.addAll(!slot.isEmpty() ? (index + 1) % (slot.size() + 1) : 0, collection);
    }

    public final List<Card> removeCards(int index) {
        GameSlot slot = getter.apply(game);
        if(this.removeAll()) {
            if (index == -1) {
                List<Card> cards = slot.stream().toList();
                slot.clear();
                return cards;
            } else {
                int i = !slot.isEmpty() ? index % slot.size() : 0;
                List<Card> cards = new ArrayList<>();
                while (i < slot.size()) {
                    cards.add(slot.remove(i));
                }
                return cards;
            }
        }else{
            Card card = index == -1 ? slot.removeLast() : slot.remove(!slot.isEmpty() ? index % slot.size() : 0);
            return List.of(card);
        }
    }
    
    public boolean removeAll() {
        return true;
    }

    public void onInsert(CardPlayer player, List<Card> cards) {

    }

    public void onRemove(CardPlayer player, List<Card> cards) {

    }

    public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
        return true;
    }

    public boolean canRemoveCard(CardPlayer player, int index) {
        GameSlot slot = getter.apply(game);
        return !slot.isEmpty();
    }

    public boolean isEmpty() {
        return getSlot().isEmpty();
    }

    public Type getType() {
        return type;
    }

    public boolean isExtended() {
        return type == Type.PREVIEW || type == Type.HORIZONTAL || type == Type.VERTICAL;
    }

    public boolean isSmall() {
        return type == Type.SMALL || type == Type.PREVIEW;
    }

    public enum Type {
        DEFAULT,
        SMALL,
        PREVIEW,
        HORIZONTAL,
        VERTICAL
    }

    public static float getWidth(CardSlot.Type type) {
        return switch (type) {
            case DEFAULT, VERTICAL -> CardImage.WIDTH * 1.5f;
            case HORIZONTAL -> 150;
            case SMALL -> CardImage.WIDTH / 2f;
            case PREVIEW -> 41;
        };
    }

    public static float getWidth(CardSlot<?> slot) {
        return getWidth(slot.getType());
    }

    public static float getHeight(CardSlot.Type type) {
        return switch (type) {
            case DEFAULT, HORIZONTAL -> CardImage.HEIGHT * 1.5f;
            case SMALL, PREVIEW -> CardImage.HEIGHT / 2f;
            case VERTICAL -> 112.5f;
        };
    }

    public static float getHeight(CardSlot<?> slot) {
        return getHeight(slot.getType());
    }

}

