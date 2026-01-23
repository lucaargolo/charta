package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.network.UpdateCardContainerCarriedPayload;
import dev.lucaargolo.charta.common.network.UpdateCardContainerSlotPayload;
import dev.lucaargolo.charta.common.utils.CardContainerSynchronizerMixed;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net/minecraft/server/level/ServerPlayer$1"})
public abstract class ContainerSynchronizerMixin implements CardContainerSynchronizerMixed {

    @Shadow @Final
    ServerPlayer this$0;

    @Override
    public void charta_sendCardSlotChange(AbstractContainerMenu container, int slot, GameSlot cards) {
        ChartaMod.getPacketManager().sendToPlayer(this$0, new UpdateCardContainerSlotPayload(container.containerId, container.incrementStateId(), slot, cards.stream().toList()));
    }

    @Override
    public void charta_sendCarriedCardsChange(AbstractContainerMenu container, GameSlot cards) {
        ChartaMod.getPacketManager().sendToPlayer(this$0, new UpdateCardContainerCarriedPayload(container.containerId, container.incrementStateId(), cards.stream().toList()));
    }

    @Inject(at = @At("TAIL"), method = "sendInitialData")
    public void charta_injectCardData(AbstractContainerMenu container, NonNullList<ItemStack> items, ItemStack carriedItem, int[] initialData, CallbackInfo ci) {
        if(container instanceof AbstractCardMenu<?, ?> menu) {
            int i = 0;
            for(int j = menu.cardSlots.size(); i < j; i++) {
                ChartaMod.getPacketManager().sendToPlayer(this$0, new UpdateCardContainerSlotPayload(container.containerId, container.stateId, i, menu.getRemoteCards(i).stream().toList()));
            }
            ChartaMod.getPacketManager().sendToPlayer(this$0, new UpdateCardContainerCarriedPayload(container.containerId, container.stateId, menu.getRemoteCarriedCards().stream().toList()));

        }

    }

}
