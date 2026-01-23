package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.FabricChartaMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {

    @Inject(at = @At("TAIL"), method = "sendChunk")
    private static void charta$onSendChunk(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        ((FabricChartaMod) ChartaMod.getInstance()).getOnChunkSent().forEach(c -> c.accept(chunk, packetListener.player));
    }

}
