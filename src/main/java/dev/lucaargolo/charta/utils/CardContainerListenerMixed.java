package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;

import java.util.List;

public interface CardContainerListenerMixed extends CardContainerListener {

    void charta_cardChanged(AbstractCardMenu cardMenu, int cardSlotIndex, List<Card> cards);

    default void cardChanged(AbstractCardMenu cardMenu, int cardSlotIndex, List<Card> cards) {
        charta_cardChanged(cardMenu, cardSlotIndex, cards);
    };

}
