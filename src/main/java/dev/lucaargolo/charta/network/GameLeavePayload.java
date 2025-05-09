package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.ConfirmScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record GameLeavePayload() implements CustomPacketPayload {

    public static final Type<GameLeavePayload> TYPE = new Type<>(Charta.id("game_leave"));

    public static StreamCodec<ByteBuf, GameLeavePayload> STREAM_CODEC = StreamCodec.unit(new GameLeavePayload());

    public static void handleBoth(GameLeavePayload payload, IPayloadContext context) {
        if(context.flow() == PacketFlow.SERVERBOUND) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleServer(GameLeavePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.player().stopRiding();
        });
    }

    public static void handleClient(GameLeavePayload payload, IPayloadContext context) {
        context.enqueueWork(GameLeavePayload::openExitScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public static void openExitScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(null, Component.translatable("message.charta.leaving_game"), true, () -> {
            if(minecraft.player != null) {
                minecraft.player.stopRiding();
                PacketDistributor.sendToServer(new GameLeavePayload());
            }
        }));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
