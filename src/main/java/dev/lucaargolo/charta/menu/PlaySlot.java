package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class PlaySlot<G extends CardGame<G>> extends CardSlot<G> {

    @Nullable
    private final DrawSlot<G> drawSlot;

    public PlaySlot(G game, Function<G, GameSlot> getter, float x, float y, @Nullable DrawSlot<G> drawSlot) {
        super(game, getter, x, y);
        this.drawSlot = drawSlot;
    }

    @Override
    public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
        if(drawSlot != null && drawSlot.isDraw()) {
            player.getPlay(this.game).complete(null);
            drawSlot.setDraw(false);
        }
        return player == this.game.getCurrentPlayer() && this.game.canPlay(player, new CardPlay(cards, getter.apply(this.game).getIndex()));
    }

    @Override
    public boolean canRemoveCard(CardPlayer player, int index) {
        return false;
    }

    @Override
    public void onInsert(CardPlayer player, List<Card> cards) {
        player.getPlay(this.game).complete(new CardPlay(cards, getter.apply(this.game).getIndex()));
    }

}
