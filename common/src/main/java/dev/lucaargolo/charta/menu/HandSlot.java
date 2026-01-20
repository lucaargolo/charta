package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.GameSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class HandSlot<G extends CardGame<G, M>, M extends AbstractCardMenu<G, M>> extends CardSlot<G, M> {

    protected final Function<G, GameSlot> censored;
    protected final Predicate<G> shouldUpdate;

    public HandSlot(G game, Predicate<G> shouldUpdate, CardPlayer player, float x, float y) {
        super(game, g -> g.getPlayerHand(player), x, y);
        this.censored = g -> g.getCensoredHand(player);
        this.shouldUpdate = shouldUpdate;
    }

    public HandSlot(G game, Predicate<G> shouldUpdate, CardPlayer player, float x, float y, Type type) {
        super(game, g -> g.getPlayerHand(player), x, y, type);
        this.censored = g -> g.getCensoredHand(player);
        this.shouldUpdate = shouldUpdate;
    }

    public final GameSlot getCensoredSlot() {
        return censored.apply(game);
    }

    @Override
    public void postUpdate() {
        super.postUpdate();
        if (shouldUpdate.test(game)) {
            GameSlot censored = this.getCensoredSlot();
            Iterable<Card> cards = this.getSlot().getCards();
            List<Card> censoredCards = new ArrayList<>();
            cards.forEach(c -> {
                censoredCards.add(Card.BLANK);
            });
            censored.setCards(censoredCards);
        }
    }

}
