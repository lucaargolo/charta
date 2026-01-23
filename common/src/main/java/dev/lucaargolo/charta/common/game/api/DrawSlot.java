package dev.lucaargolo.charta.common.game.api;

import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.game.api.game.Game;

import java.util.List;
import java.util.function.Supplier;

public class DrawSlot extends GameSlot {

    private final Game<?, ?> game;
    private final Supplier<Boolean> canDraw;
    private boolean draw = false;

    public DrawSlot(Game<?, ?> game, List<Card> cards, float x, float y, float z, float angle, Supplier<Boolean> canDraw) {
        super(cards, x, y, z, angle);
        this.game = game;
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
    public void onRemove(CardPlayer player, List<Card> card, int index) {
        super.onRemove(player, card, index);
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
