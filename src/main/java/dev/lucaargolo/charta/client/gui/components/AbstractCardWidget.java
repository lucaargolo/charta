package dev.lucaargolo.charta.client.gui.components;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lucaargolo.charta.client.CardRenderType;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCardWidget extends AbstractPreciseWidget implements TickableWidget, HoverableRenderable {

    @Nullable
    private final HoverableRenderable parent;
    private final ResourceLocation cardId;

    private float lastInset = 0f;
    private float inset = 0f;
    private float lastFov = 30f;
    private float fov = 30f;
    private float lastXRot = 0f;
    private float xRot = 0f;
    private float lastYRot = 0f;
    private float yRot = 0f;

    public AbstractCardWidget(@Nullable HoverableRenderable parent, ResourceLocation cardId, float x, float y, float scale) {
        super(x, y, CardImage.WIDTH * 1.5f * scale, CardImage.HEIGHT * 1.5f * scale, Component.empty());
        this.parent = parent;
        this.cardId = cardId;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float inset = Mth.lerp(partialTick, this.lastInset, this.inset);
        float fov = Mth.lerp(partialTick, this.lastFov, this.fov);
        float xRot = Mth.lerp(partialTick, this.lastXRot, this.xRot);
        float yRot = Mth.lerp(partialTick, this.lastYRot, this.yRot);

        ResourceLocation textureId = getCardTexture(cardId);
        RenderType renderType = CardRenderType.getCardType(textureId);

        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(renderType);
        PoseStack.Pose pose = guiGraphics.pose().last();

        ChartaClient.CARD_INSET.set(inset);
        ChartaClient.CARD_FOV.set(fov);
        ChartaClient.CARD_X_ROT.set(xRot);
        ChartaClient.CARD_Y_ROT.set(yRot);

        float xOffset = (this.getPreciseWidth()*1.333333f - this.getPreciseWidth())/2f;
        float yOffset = (this.getPreciseHeight()*1.333333f - this.getPreciseHeight())/2f;
        drawCard(pose, consumer, this.getPreciseX()-xOffset, this.getPreciseY()-yOffset, this.getPreciseX()+this.getPreciseWidth()+xOffset, this.getPreciseY()+this.getPreciseHeight()+yOffset);

        guiGraphics.bufferSource().endBatch(renderType);

        lastInset = inset;
        lastFov = fov;
        lastXRot = xRot;
        lastYRot = yRot;
    }

    private void drawCard(PoseStack.Pose pose, VertexConsumer consumer, float x1, float y1, float x2, float y2) {
        consumer.addVertex(pose, x1, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x1, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
    }

    public void tick(int mouseX, int mouseY) {
        float xDif = ((this.getPreciseX() + this.getPreciseWidth() - mouseX)-(this.getPreciseWidth()/2f))/(this.getPreciseWidth()/2f);
        float yDif = ((this.getPreciseY() + this.getPreciseHeight() - mouseY)-(this.getPreciseHeight()/2f))/(this.getPreciseHeight()/2f);

        if(isHovered()) {
            inset = -30f;
            fov = 30f;
            xRot = yDif*25f;
            yRot = xDif*-25f;
        }else{
            inset = 0f;
            fov = 30f;
            xRot = 0f;
            yRot = 0f;
        }
    }

    @Override
    public boolean isHovered() {
        return parent != null && parent.getHoverable() == this;
    }

    public abstract ResourceLocation getCardTexture(ResourceLocation cardId);

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }


}
