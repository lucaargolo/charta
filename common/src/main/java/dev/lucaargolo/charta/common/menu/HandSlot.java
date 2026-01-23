package dev.lucaargolo.charta.common.menu;

import dev.lucaargolo.charta.common.game.api.CardPlayer;
import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.game.api.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class HandSlot<G extends Game<G, M>, M extends AbstractCardMenu<G, M>> extends CardSlot<G, M> {

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
                censoredCards.add(new Card());
            });
            censored.setCards(censoredCards);
        }
    }

}
