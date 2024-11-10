package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.world.inventory.ContainerListener;

import java.util.List;

public interface CardContainerListener extends ContainerListener {

    void cardChanged(AbstractCardMenu cardMenu, int cardSlotIndex, List<Card> cards);


}
