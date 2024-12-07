package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DrawSlot<G extends CardGame<G>> extends CardSlot<G> {

    private final Supplier<Boolean> canDraw;
    private boolean draw = false;

    public DrawSlot(G game, Function<G, GameSlot> getter, float x, float y, Supplier<Boolean> canDraw) {
        super(game, getter, x, y);
        this.canDraw = canDraw;
    }

    @Override
    public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
        return false;
    }

    @Override
    public boolean canRemoveCard(CardPlayer player, int index) {
        return !draw && player == this.game.getCurrentPlayer() && canDraw.get();
    }

    @Override
    public void onRemove(CardPlayer player, List<Card> card) {
        super.onRemove(player, card);
        card.forEach(Card::flip);
        draw = true;
    }

    @Override
    public boolean removeAll() {
        return false;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }
}
