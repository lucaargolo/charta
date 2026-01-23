package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.utils.CardContainerListenerMixed;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {"net/minecraft/server/level/ServerPlayer$2"})
public abstract class ContainerListenerMixin implements CardContainerListenerMixed {

    @Override
    public void charta_cardChanged(AbstractCardMenu<?, ?> cardMenu, int cardSlotIndex, GameSlot cards) {

    }

}
