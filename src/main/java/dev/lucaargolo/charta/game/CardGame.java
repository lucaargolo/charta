package dev.lucaargolo.charta.game;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public interface CardGame {

    List<Card> getValidDeck();

    List<CardPlayer> getPlayers();

    List<Card> getCensoredHand(CardPlayer player);

    CardPlayer getCurrentPlayer();

    CardPlayer getNextPlayer();

    void startGame();

    void runGame();

    boolean canPlayCard(CardPlayer player, Card card);

    @Nullable Card getBestCard(CardPlayer cards);

    boolean isGameOver();

    CardPlayer getWinner();

    default void tick() {
        getPlayers().forEach(p -> p.tick(this));
    }

    default int getMinPlayers() {
        return 2;
    }

    default int getMaxPlayers() {
        return 8;
    }

    static boolean canPlayGame(CardGame cardGame, CardDeck cardDeck) {
        List<Card> necessaryCards = cardGame.getValidDeck();
        cardDeck.getCards().forEach(necessaryCards::remove);
        return necessaryCards.isEmpty();
    }

    static void dealCards(LinkedList<Card> drawPile, CardPlayer player, List<Card> censoredHand, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawPile.pollLast();
            card.flip();
            player.getHand().add(card);
            censoredHand.add(Card.BLANK);
        }
    }

}