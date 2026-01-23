package dev.lucaargolo.charta.common.utils;

import dev.lucaargolo.charta.common.game.api.GameSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface CardContainerSynchronizerMixed extends CardContainerSynchronizer {

    void charta_sendCardSlotChange(AbstractContainerMenu container, int slot, GameSlot cards);

    void charta_sendCarriedCardsChange(AbstractContainerMenu container, GameSlot cards);

    default void sendCardSlotChange(AbstractContainerMenu container, int slot, GameSlot cards) {
        charta_sendCardSlotChange(container, slot, cards);
    }

    default void sendCarriedCardsChange(AbstractContainerMenu container, GameSlot cards) {
        charta_sendCarriedCardsChange(container, cards);
    }


}
