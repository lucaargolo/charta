package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.Card;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;

import java.util.List;

public interface CardContainerSynchronizer extends ContainerSynchronizer {

    void sendCardSlotChange(AbstractContainerMenu container, int slot, List<Card> cards);

    void sendCarriedCardsChange(AbstractContainerMenu container, List<Card> cards);

}
