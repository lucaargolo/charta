package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PlaySlot<G extends CardGame<G>> extends CardSlot<G> {

    @Nullable
    private final DrawSlot<G> drawSlot;

    public PlaySlot(G game, Function<G, GameSlot> getter, float x, float y, @Nullable DrawSlot<G> drawSlot) {
        super(game, getter, x, y);
        this.drawSlot = drawSlot;
    }

    @Override
    public boolean canInsertCard(CardPlayer player, Iterable<Card> cards) {
        int size = 0;
        Card lastCard = null;
        for(Card card : cards) {
            size++;
            lastCard = card;
        }
        if(drawSlot != null && drawSlot.isDraw()) {
            player.getPlay(this.game).complete(null);
            drawSlot.setDraw(false);
        }
        return player == this.game.getCurrentPlayer() && size == 1 && this.game.canPlay(player, new CardPlay(lastCard, getter.apply(this.game).getIndex()));
    }

    @Override
    public boolean canRemoveCard(CardPlayer player) {
        return false;
    }

    @Override
    public void onInsert(CardPlayer player, Card card) {
        player.getPlay(this.game).complete(new CardPlay(card, getter.apply(this.game).getIndex()));
    }

}
