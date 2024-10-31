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

public abstract class AbstractCardWidget extends AbstractWidget implements TickableWidget, HoverableRenderable {

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

    public AbstractCardWidget(@Nullable HoverableRenderable parent, ResourceLocation cardId, int x, int y, float scale) {
        super(x, y, (int) (CardImage.WIDTH * 1.5f * scale), (int) (CardImage.HEIGHT * 1.5f * scale), Component.empty());
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

        int xOffset = (int) ((this.getWidth()*1.333333f - this.getWidth())/2f);
        int yOffset = (int) ((this.getHeight()*1.333333f - this.getHeight())/2f);
        drawCard(pose, consumer, this.getX()-xOffset, this.getY()-yOffset, this.getX()+this.getWidth()+xOffset, this.getY()+this.getHeight()+yOffset);

        guiGraphics.bufferSource().endBatch(renderType);

        lastInset = inset;
        lastFov = fov;
        lastXRot = xRot;
        lastYRot = yRot;
    }

    private void drawCard(PoseStack.Pose pose, VertexConsumer consumer, int x1, int y1, int x2, int y2) {
        consumer.addVertex(pose, x1, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x1, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
    }

    public void tick(int mouseX, int mouseY) {
        float xDif = ((this.getX() + this.getWidth() - mouseX)-(this.getWidth()/2f))/(this.getWidth()/2f);
        float yDif = ((this.getY() + this.getHeight() - mouseY)-(this.getHeight()/2f))/(this.getHeight()/2f);

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
