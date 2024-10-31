package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.Card;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public interface CardContainerSynchronizerMixed extends CardContainerSynchronizer {

    void charta_sendCardSlotChange(AbstractContainerMenu container, int slot, List<Card> cards);

    void charta_sendCarriedCardsChange(AbstractContainerMenu container, List<Card> cards);

    default void sendCardSlotChange(AbstractContainerMenu container, int slot, List<Card> cards) {
        charta_sendCardSlotChange(container, slot, cards);
    };

    default void sendCarriedCardsChange(AbstractContainerMenu container, List<Card> cards) {
        charta_sendCarriedCardsChange(container, cards);
    };


}
