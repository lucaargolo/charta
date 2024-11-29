package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class PlaySlot<G extends CardGame<G>> extends CardSlot<G> {

    @Nullable
    private final DrawSlot<G> drawSlot;

    public PlaySlot(G game, Function<G, List<Card>> getter, float x, float y, @Nullable DrawSlot<G> drawSlot) {
        super(game, getter, x, y);
        this.drawSlot = drawSlot;
    }

    @Override
    public boolean canInsertCard(CardPlayer player, List<Card> cards) {
        if(drawSlot != null && drawSlot.isDraw()) {
            player.getPlay(this.game).complete(null);
            drawSlot.setDraw(false);
        }
        return player == this.game.getCurrentPlayer() && cards.size() == 1 && this.game.canPlayCard(player, cards.getLast());
    }

    @Override
    public boolean canRemoveCard(CardPlayer player) {
        return false;
    }

    @Override
    public void onInsert(CardPlayer player, Card card) {
        player.getPlay(this.game).complete(card);
    }

}
