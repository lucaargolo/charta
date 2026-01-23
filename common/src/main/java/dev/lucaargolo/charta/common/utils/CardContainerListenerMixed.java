package dev.lucaargolo.charta.common.utils;

import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;

public interface CardContainerListenerMixed extends CardContainerListener {

    void charta_cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards);

    default void cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards) {
        charta_cardChanged(cardMenu, cardSlotIndex, cards);
    }

}
