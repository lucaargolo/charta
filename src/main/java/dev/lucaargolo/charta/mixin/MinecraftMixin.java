package dev.lucaargolo.charta.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Final private Window window;

    @Inject(at = @At("TAIL"), method = "resizeDisplay")
    public void resizeDisplay(CallbackInfo ci) {
        RenderTarget blurRenderTarget = ChartaClient.getBlurRenderTarget();
        if(blurRenderTarget != null)
            blurRenderTarget.resize(this.window.getWidth(), this.window.getHeight(), Minecraft.ON_OSX);
        PostChain blurEffect = ChartaClient.getBlurEffect();
        if(blurEffect != null)
            blurEffect.resize(this.window.getWidth(), this.window.getHeight());

        RenderTarget glowRenderTarget = ChartaClient.getGlowRenderTarget();
        if(glowRenderTarget != null)
            glowRenderTarget.resize(this.window.getWidth(), this.window.getHeight(), Minecraft.ON_OSX);
        PostChain glowBlurEffect = ChartaClient.getGlowBlurEffect();
        if(glowBlurEffect != null)
            glowBlurEffect.resize(this.window.getWidth(), this.window.getHeight());
    }


}
