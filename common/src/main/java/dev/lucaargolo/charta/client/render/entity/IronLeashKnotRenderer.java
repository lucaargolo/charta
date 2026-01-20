package dev.lucaargolo.charta.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.entity.IronLeashFenceKnotEntity;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import org.jetbrains.annotations.NotNull;

public class IronLeashKnotRenderer extends EntityRenderer<IronLeashFenceKnotEntity> {

    private static final ResourceLocation IRON_KNOT_LOCATION = ChartaMod.id("textures/entity/iron_lead_knot.png");
    public final LeashKnotModel<LeashFenceKnotEntity> model;

    public IronLeashKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel<>(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    public void render(@NotNull IronLeashFenceKnotEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(IRON_KNOT_LOCATION));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull IronLeashFenceKnotEntity entity) {
        return IRON_KNOT_LOCATION;
    }

}
