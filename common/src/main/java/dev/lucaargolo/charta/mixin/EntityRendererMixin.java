package dev.lucaargolo.charta.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.mixed.LeashableMixed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Unique
    private Entity charta_capturedLeashable;

    @Inject(at = @At("HEAD"), method = "renderLeash")
    public <E extends Entity> void captureLeashable(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder, CallbackInfo ci) {
        charta_capturedLeashable = entity;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), method = "renderLeash")
    public RenderType getIronLeash(RenderType renderType) {
        if(charta_capturedLeashable instanceof LeashableMixed mixed && mixed.charta_isIronLeash()) {
            return ChartaModClient.getRenderTypeManager().ironLeash();
        }
        return renderType;
    }


}
