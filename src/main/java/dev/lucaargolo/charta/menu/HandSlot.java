package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HandSlot<G extends CardGame<G>> extends CardSlot<G> {

    protected final Function<G, GameSlot> censored;

    public HandSlot(G game, CardPlayer player, float x, float y) {
        super(game, g -> g.getPlayerHand(player), x, y);
        this.censored = g -> g.getCensoredHand(player);
    }

    public HandSlot(G game, CardPlayer player, float x, float y, Type type) {
        super(game, g -> g.getPlayerHand(player), x, y, type);
        this.censored = g -> g.getCensoredHand(player);
    }

    public final GameSlot getCensoredSlot() {
        return censored.apply(game);
    }

    @Override
    protected void onUpdate() {
        GameSlot censored = this.getCensoredSlot();
        Iterable<Card> cards = this.getSlot().getCards();
        List<Card> censoredCards = new ArrayList<>();
        cards.forEach(c -> {
            censoredCards.add(Card.BLANK);
        });
        censored.setCards(censoredCards);
    }

}
