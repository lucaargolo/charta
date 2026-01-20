package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;

public interface CardContainerListenerMixed extends CardContainerListener {

    void charta_cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards);

    default void cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards) {
        charta_cardChanged(cardMenu, cardSlotIndex, cards);
    }

}
