package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.GameSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;

public interface CardContainerSynchronizer extends ContainerSynchronizer {

    void sendCardSlotChange(AbstractContainerMenu container, int slot, GameSlot cards);

    void sendCarriedCardsChange(AbstractContainerMenu container, GameSlot cards);

}
