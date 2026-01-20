package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.world.inventory.ContainerListener;

public interface CardContainerListener extends ContainerListener {

    void cardChanged(AbstractCardMenu<?> cardMenu, int cardSlotIndex, GameSlot cards);

}
