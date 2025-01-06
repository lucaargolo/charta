package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.ConfirmScreen;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record GameLeavePayload() implements CustomPacketPayload {

    public static final Type<GameLeavePayload> TYPE = new Type<>(Charta.id("game_leave"));

    public static StreamCodec<ByteBuf, GameLeavePayload> STREAM_CODEC = StreamCodec.unit(new GameLeavePayload());

    public static void handleServer(Player player, GameLeavePayload payload) {
        player.stopRiding();
    }

    public static void handleClient(Player player, GameLeavePayload payload) {
        openExitScreen();
    }

    @Environment(EnvType.CLIENT)
    public static void openExitScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmScreen(null, Component.translatable("message.charta.leaving_game"), true, () -> {
            if(minecraft.player != null) {
                minecraft.player.stopRiding();
                ClientPlayNetworking.send(new GameLeavePayload());
            }
        }));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
