package dev.lucaargolo.charta.common.utils;

import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import net.minecraft.world.inventory.ContainerListener;

public interface CardContainerListener extends ContainerListener {

    void cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards);

}
