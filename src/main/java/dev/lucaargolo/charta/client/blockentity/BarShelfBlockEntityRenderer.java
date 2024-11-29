package dev.lucaargolo.charta.client.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lucaargolo.charta.block.BarShelfBlock;
import dev.lucaargolo.charta.blockentity.BarShelfBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class BarShelfBlockEntityRenderer implements BlockEntityRenderer<BarShelfBlockEntity> {

    private final BlockEntityRendererProvider.Context context;

    public BarShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@NotNull BarShelfBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer renderer = context.getItemRenderer();
        poseStack.pushPose();
        Direction facing = blockEntity.getBlockState().getValue(BarShelfBlock.FACING);
        switch (facing) {
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270f));
                poseStack.translate(0.015, 0.0, -1.0);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
                poseStack.translate(-0.985, 0.0, -1.0);

            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                poseStack.translate(-0.985, 0.0, 0.0);
            }
            default -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(0f));
                poseStack.translate(0.015, 0.0, 0.0);
            }
        }

        poseStack.scale(0.85f, 0.825f, 0.85f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.translate(-0.925, 0.8, 0.0);
        if(blockEntity.getItem(0).getItem() instanceof BlockItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            poseStack.translate(-0.1, -0.18, 0.0);
        }
        renderer.renderStatic(blockEntity.getItem(0), ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
        if(blockEntity.getItem(0).getItem() instanceof BlockItem) {
            poseStack.translate(0.1, 0.18, 0.0);
            poseStack.mulPose(Axis.YN.rotationDegrees(90f));
        }
        poseStack.translate(0.675, 0.0, 0.0);
        if(blockEntity.getItem(1).getItem() instanceof BlockItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            poseStack.translate(-0.1, -0.18, 0.0);
        }
        renderer.renderStatic(blockEntity.getItem(1), ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
        if(blockEntity.getItem(1).getItem() instanceof BlockItem) {
            poseStack.translate(0.1, 0.18, 0.0);
            poseStack.mulPose(Axis.YN.rotationDegrees(90f));
        }
        poseStack.translate(-0.675, -0.6f, 0.0);
        if(blockEntity.getItem(2).getItem() instanceof BlockItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            poseStack.translate(-0.1, -0.18, 0.0);
        }
        renderer.renderStatic(blockEntity.getItem(2), ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
        if(blockEntity.getItem(2).getItem() instanceof BlockItem) {
            poseStack.translate(0.1, 0.18, 0.0);
            poseStack.mulPose(Axis.YN.rotationDegrees(90f));
        }
        poseStack.translate(0.675, 0.0, 0.0);
        if(blockEntity.getItem(3).getItem() instanceof BlockItem) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            poseStack.translate(-0.1, -0.18, 0.0);
        }
        renderer.renderStatic(blockEntity.getItem(3), ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
        if(blockEntity.getItem(3).getItem() instanceof BlockItem) {
            poseStack.translate(0.1, 0.18, 0.0);
            poseStack.mulPose(Axis.YN.rotationDegrees(90f));
        }
        poseStack.popPose();
    }

}
