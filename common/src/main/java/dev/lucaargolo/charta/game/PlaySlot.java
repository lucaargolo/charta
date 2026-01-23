package dev.lucaargolo.charta.game;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaySlot extends GameSlot {

    private final Game<?, ?> game;
    @Nullable
    private final DrawSlot drawSlot;

    public PlaySlot(Game<?, ?> game, List<Card> cards, float x, float y, float z, float angle, @Nullable DrawSlot drawSlot) {
        super(cards, x, y, z, angle);
        this.game = game;
        this.drawSlot = drawSlot;
    }

    @Override
    public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
        if(drawSlot != null && drawSlot.isDraw()) {
            player.play(null);
            drawSlot.setDraw(false);
        }
        return player == this.game.getCurrentPlayer() && this.game.canPlay(player, new CardPlay(cards, this.getIndex()));
    }

    @Override
    public boolean canRemoveCard(CardPlayer player, int index) {
        return false;
    }

    @Override
    public void onInsert(CardPlayer player, List<Card> cards, int index) {
        player.play(cards, this.getIndex());
    }

}
