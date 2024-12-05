package dev.lucaargolo.charta.client.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.client.ModRenderType;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class CardTableBlockEntityRenderer implements BlockEntityRenderer<CardTableBlockEntity> {

    private final BlockEntityRendererProvider.Context context;

    public CardTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@NotNull CardTableBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        CardDeck deck = blockEntity.getDeck();
        ItemStack deckStack = blockEntity.getDeckStack();
        poseStack.pushPose();
        poseStack.translate(0.0, 0.85, 1.0);
        poseStack.mulPose(Axis.XN.rotationDegrees(90f));
        int gameSlots = blockEntity.getSlotCount();
        if(deck != null && gameSlots > 0) {
            for(int i = 0; i < gameSlots; i++) {
                GameSlot slot = blockEntity.getSlot(i);
                float x = slot.lerpX(partialTick);
                float y = slot.lerpY(partialTick);
                float z = slot.lerpZ(partialTick);
                float angle = slot.lerpAngle(partialTick);
                Direction stackDirection = slot.getStackDirection();

                int cards = slot.size();

                float maxWidth = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? 0 : slot.getMaxStack();
                float cardWidth = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? 0 : CardImage.WIDTH;
                float maxLeftOffset = cardWidth + cardWidth / 10f;

                float maxHeight = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? slot.getMaxStack() : 0;
                float cardHeight = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? CardImage.HEIGHT : 0;
                float maxTopOffset = cardHeight + cardHeight / 10f;

                float left = 0f, leftOffset;
                float top = 0f, topOffset;

                if(slot.isCentered()) {
                    leftOffset = cardWidth + Math.max(0f, maxWidth - (cards * cardWidth) / (float) cards);
                    float totalWidth = cardWidth + (leftOffset * (cards - 1f));
                    float leftExcess = totalWidth - maxWidth;
                    if (leftExcess > 0) {
                        leftOffset -= leftExcess / (cards - 1f);
                    }

                    totalWidth = cardWidth + (maxLeftOffset * (cards - 1f));
                    left = 0;
                    if (leftOffset > maxLeftOffset) {
                        left = Math.max(leftOffset - maxLeftOffset, (maxWidth - totalWidth));
                        leftOffset = maxLeftOffset;
                    }

                    topOffset = cardHeight + Math.max(0f, maxHeight - (cards * cardHeight) / (float) cards);
                    float totalHeight = cardHeight + (topOffset * (cards - 1f));
                    float topExcess = totalHeight - maxHeight;
                    if (topExcess > 0) {
                        topOffset -= topExcess / (cards - 1f);
                    }

                    totalHeight = cardHeight + (maxTopOffset * (cards - 1f));
                    top = 0;
                    if (topOffset > maxTopOffset) {
                        top = Math.max(topOffset - maxTopOffset, (maxHeight - totalHeight));
                        topOffset = maxTopOffset;
                    }
                }else{
                    leftOffset = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? 0f : 5f;
                    if(leftOffset * (slot.size() - 1) + cardWidth > maxWidth) {
                        leftOffset = (maxWidth - cardWidth) / (slot.size() - 1);
                    }
                    topOffset = stackDirection.getAxis().isVertical() ? 0 : angle % 180 == 0 && stackDirection.getAxis() == Direction.Axis.Z ? 7.5f : 0f;
                    if(topOffset * (slot.size() - 1) + cardHeight > maxHeight) {
                        topOffset = (maxHeight - cardHeight) / (slot.size() - 1);
                    }
                }

                Vector3f normal = new Vector3f(0f, -1f, 0f);
                normal.rotateAxis(90f, -1f, 0f, 0f);
                normal.rotateAxis(angle, 0f, 0f, -1f);

                int o = 0;
                for(Card card : slot.getCards()) {
                    poseStack.pushPose();
                    poseStack.scale(1/160f, 1/160f, 1/160f);
                    poseStack.translate(x, y, z + (o*0.01f));
                    if(stackDirection.getAxis().isVertical()) {
                        poseStack.translate(0, 0, (o*0.25f)*stackDirection.getNormal().getY());
                    }
                    poseStack.mulPose(Axis.ZN.rotationDegrees(angle));
                    poseStack.translate(o*leftOffset, o*topOffset, 0.0);
                    poseStack.scale(160f, 160f, 160f);
                    if(slot.isCentered()) {
                        drawCard(deck, card, packedLight, packedOverlay, poseStack, bufferSource, left / 2, top / 2, normal);
                    }else{
                        drawCard(deck, card, packedLight, packedOverlay, poseStack, bufferSource, 0f, 0f, normal);
                    }
                    poseStack.popPose();
                    o++;
                }
            }
        }else if(!deckStack.isEmpty()) {
            poseStack.translate(0.5 + blockEntity.centerOffset.x, 0.275 + blockEntity.centerOffset.y, 0.0);
            context.getItemRenderer().renderStatic(deckStack, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), 1);
        }
        poseStack.popPose();
    }

    private void drawCard(CardDeck deck, Card card, int packedLight, int packedOverlay, PoseStack poseStack, MultiBufferSource bufferSource, float x, float y, Vector3f normal) {
        PoseStack.Pose entry = poseStack.last();

        if(IrisCompat.isPresent()) {
            ResourceLocation glowTexture = card.isFlipped() ? deck.getDeckTexture(true) : deck.getCardTexture(card, true);
            RenderType glowType = RenderType.entityTranslucentEmissive(glowTexture);
            VertexConsumer glowConsumer = bufferSource.getBuffer(glowType);
            glowConsumer.addVertex(entry.pose(), (x+CardImage.WIDTH)/160f, y/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(1f, 1f).setOverlay(packedOverlay).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, normal.x, normal.y, normal.z);
            glowConsumer.addVertex(entry.pose(), (x+CardImage.WIDTH)/160f, (y+CardImage.HEIGHT)/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(1f, 0f).setOverlay(packedOverlay).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, normal.x, normal.y, normal.z);
            glowConsumer.addVertex(entry.pose(), x/160f, (y+CardImage.HEIGHT)/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(0f, 0f).setOverlay(packedOverlay).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, normal.x, normal.y, normal.z);
            glowConsumer.addVertex(entry.pose(), x/160f, y/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(0f, 1f).setOverlay(packedOverlay).setLight(LightTexture.FULL_BRIGHT).setNormal(entry, normal.x, normal.y, normal.z);
        }

        ResourceLocation texture = card.isFlipped() ? deck.getDeckTexture(false) : deck.getCardTexture(card, false);
        RenderType type = IrisCompat.isPresent() ? RenderType.entityTranslucent(texture) : ModRenderType.entityCard(texture);
        VertexConsumer consumer = bufferSource.getBuffer(type);

        consumer.addVertex(entry.pose(), (x+CardImage.WIDTH)/160f, y/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(1f, 1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(entry, normal.x, normal.y, normal.z);
        consumer.addVertex(entry.pose(), (x+CardImage.WIDTH)/160f, (y+CardImage.HEIGHT)/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(1f, 0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(entry, normal.x, normal.y, normal.z);
        consumer.addVertex(entry.pose(), x/160f, (y+CardImage.HEIGHT)/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(0f, 0f).setOverlay(packedOverlay).setLight(packedLight).setNormal(entry, normal.x, normal.y, normal.z);
        consumer.addVertex(entry.pose(), x/160f, y/160f, 0).setColor(1f, 1f, 1f, 1f).setUv(0f, 1f).setOverlay(packedOverlay).setLight(packedLight).setNormal(entry, normal.x, normal.y, normal.z);
    }

}
