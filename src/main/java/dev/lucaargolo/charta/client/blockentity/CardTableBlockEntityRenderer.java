package dev.lucaargolo.charta.client.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CardTableBlockEntityRenderer implements BlockEntityRenderer<CardTableBlockEntity> {

    private final BlockEntityRendererProvider.Context context;

    public CardTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@NotNull CardTableBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack deckStack = blockEntity.getDeckStack();
        if(!deckStack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.85, 0.7);
            poseStack.mulPose(Axis.XN.rotationDegrees(90f));
            context.getItemRenderer().renderStatic(deckStack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
            poseStack.popPose();
        }
    }

}
