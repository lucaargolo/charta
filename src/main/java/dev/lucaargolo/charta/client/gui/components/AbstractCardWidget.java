package dev.lucaargolo.charta.client.gui.components;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCardWidget extends AbstractPreciseWidget implements TickableWidget, HoverableRenderable {

    @Nullable
    private final HoverableRenderable parent;
    @Nullable
    private final ResourceLocation cardId;
    @Nullable
    private final String cardTranslatableKey;
    private final int cardColor;

    private float lastInset = 0f;
    private float inset = 0f;
    private float lastFov = 30f;
    private float fov = 30f;
    private float lastXRot = 0f;
    private float xRot = 0f;
    private float lastYRot = 0f;
    private float yRot = 0f;

    public AbstractCardWidget(@Nullable HoverableRenderable parent, @Nullable ResourceLocation cardId, @Nullable String cardTranslatableKey, int cardColor, float x, float y, float scale) {
        super(x, y, CardImage.WIDTH * 1.5f * scale, CardImage.HEIGHT * 1.5f * scale, Component.empty());
        this.parent = parent;
        this.cardId = cardId;
        this.cardTranslatableKey = cardTranslatableKey;
        this.cardColor = cardColor;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(isHovered && this.getCardTranslatableKey() != null) {
            scheduleTooltip(Component.translatable(this.getCardTranslatableKey()).withStyle(s -> s.withColor(this.getCardColor())));
        }

        float inset = Mth.lerp(partialTick, this.lastInset, this.inset);
        float fov = Mth.lerp(partialTick, this.lastFov, this.fov);
        float xRot = Mth.lerp(partialTick, this.lastXRot, this.xRot);
        float yRot = Mth.lerp(partialTick, this.lastYRot, this.yRot);

        ChartaClient.CARD_INSET.accept(inset);
        ChartaClient.CARD_FOV.accept(fov);
        ChartaClient.CARD_X_ROT.accept(xRot);
        ChartaClient.CARD_Y_ROT.accept(yRot);

        float xOffset = (this.getPreciseWidth()*1.333333f - this.getPreciseWidth())/2f;
        float yOffset = (this.getPreciseHeight()*1.333333f - this.getPreciseHeight())/2f;
        ChartaGuiGraphics.blitCard(guiGraphics, this.getCardTexture(cardId, false), this.getPreciseX()-xOffset, this.getPreciseY()-yOffset, this.getPreciseWidth()+(xOffset*2f), this.getPreciseHeight()+(yOffset*2f));
        ChartaClient.getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        ChartaGuiGraphics.blitCard(guiGraphics, this.getCardTexture(cardId, false), this.getPreciseX()-xOffset, this.getPreciseY()-yOffset, this.getPreciseWidth()+(xOffset*2f), this.getPreciseHeight()+(yOffset*2f));
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitCardGlow(guiGraphics, this.getCardTexture(cardId, true), this.getPreciseX()-xOffset, this.getPreciseY()-yOffset, this.getPreciseWidth()+(xOffset*2f), this.getPreciseHeight()+(yOffset*2f));
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

        lastInset = inset;
        lastFov = fov;
        lastXRot = xRot;
        lastYRot = yRot;
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
    public void scheduleTooltip(Component component) {
        if(parent != null)
            parent.scheduleTooltip(component);
    }

    @Override
    public boolean isHovered() {
        return parent != null && parent.getHoverable() == this;
    }

    @NotNull
    public abstract ResourceLocation getCardTexture(@Nullable ResourceLocation cardId, boolean glow);

    @Nullable
    public String getCardTranslatableKey() {
        return cardTranslatableKey;
    }

    public int getCardColor() {
        return cardColor;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }


}
