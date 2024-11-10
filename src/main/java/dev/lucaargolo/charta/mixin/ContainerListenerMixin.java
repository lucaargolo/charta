package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.utils.CardContainerListenerMixed;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(targets = {"net/minecraft/server/level/ServerPlayer$2"})
public abstract class ContainerListenerMixin implements CardContainerListenerMixed {

    @Shadow @Final
    ServerPlayer this$0;

    @Override
    public void charta_cardChanged(AbstractCardMenu cardMenu, int cardSlotIndex, List<Card> cards) {

    }

}
