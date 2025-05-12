package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.fun.FunScreen;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class LastFunPayload implements CustomPacketPayload {

    private final ItemStack deckStack;

    public LastFunPayload(ItemStack deckStack) {
        this.deckStack = deckStack;
    }

    public LastFunPayload() {
        this(ItemStack.EMPTY);
    }

    public LastFunPayload(FriendlyByteBuf buf) {
        this.deckStack = buf.readItem();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(deckStack);
    }

    public static void handleBoth(LastFunPayload payload, NetworkEvent.Context context) {
        if(context.getDirection().getReceptionSide().isServer()) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleServer(LastFunPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof FunMenu funMenu) {
                funMenu.getGame().sayLast(((LivingEntityMixed) player).charta_getCardPlayer());
            }
        });
    }

    public static void handleClient(LastFunPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> displayTotemEffect(payload.deckStack));
    }

    @OnlyIn(Dist.CLIENT)
    public static void displayTotemEffect(ItemStack deckStack) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null && mc.screen instanceof FunScreen funScreen) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.TOTEM_USE, mc.player.getSoundSource(), 1.0F, 1.0F, false);
            funScreen.displayItemActivation(deckStack);
        }
    }

}
