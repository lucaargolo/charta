package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.sound.ModSounds;

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
    public boolean canInsertCard(CardPlayer player, Iterable<Card> cards) {
        return false;
    }

    @Override
    public boolean canRemoveCard(CardPlayer player) {
        return !draw && player == this.game.getCurrentPlayer() && canDraw.get();
    }

    @Override
    public void onRemove(CardPlayer player, Card card) {
        player.playSound(ModSounds.CARD_DRAW.get());
        card.flip();
        draw = true;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }
}
