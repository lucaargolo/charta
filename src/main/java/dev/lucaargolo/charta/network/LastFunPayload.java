package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.menu.FunMenu;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record LastFunPayload(ItemStack deckStack) implements CustomPacketPayload {

    public LastFunPayload() {
        this(ItemStack.EMPTY);
    }

    public static final Type<LastFunPayload> TYPE = new Type<>(Charta.id("last_fun"));

    private static final StreamCodec<ByteBuf, ItemStack> STACK_STREAM = ByteBufCodecs.fromCodecTrusted(ItemStack.OPTIONAL_CODEC);
    public static StreamCodec<ByteBuf, LastFunPayload> STREAM_CODEC = StreamCodec.composite(
            STACK_STREAM,
            LastFunPayload::deckStack,
            LastFunPayload::new
    );

    public static void handleBoth(LastFunPayload payload, IPayloadContext context) {
        if(context.flow() == PacketFlow.SERVERBOUND) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleServer(LastFunPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if(player instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof FunMenu funMenu) {
                funMenu.getGame().sayLast(((LivingEntityMixed) player).charta_getCardPlayer());
            }
        });
    }

    public static void handleClient(LastFunPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> displayTotemEffect(payload.deckStack));
    }

    @OnlyIn(Dist.CLIENT)
    public static void displayTotemEffect(ItemStack deckStack) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.TOTEM_USE, mc.player.getSoundSource(), 1.0F, 1.0F, false);
            mc.gameRenderer.displayItemActivation(deckStack);
        }
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
