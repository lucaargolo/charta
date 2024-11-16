package dev.lucaargolo.charta.client.gui.screens;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CardScreen extends Screen implements HoverableRenderable {

    private HoverableRenderable hoverable = null;

    protected CardScreen(Component title) {
        super(title);
    }

    protected abstract void renderFg(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY);

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ChartaClient.getGlowRenderTarget().clear(Minecraft.ON_OSX);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width/2, 20, 0xFFFFFFFF);
        for (Renderable renderable : this.renderables) {
            if(renderable != this.hoverable) {
                /*
                This method call might seem weird at first, but by rendering the
                current hoverable before each other possible hoverable, we stop
                the cards from flashing weirdly on certain changing cases.
                 */
                if(this.hoverable != null) {
                    this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                if(renderable instanceof AbstractWidget widget && renderable instanceof HoverableRenderable) {
                    if(widget.isHovered && (!(this.hoverable instanceof AbstractWidget other) || !other.isHovered)){
                        this.hoverable = (HoverableRenderable) renderable;
                    }
                }
            }
        }
        if(this.hoverable != null) {
            this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
            if(this.hoverable instanceof AbstractWidget widget && !widget.isHovered) {
                this.hoverable = null;
            }
        }
        renderFg(guiGraphics, mouseX, mouseY);
        renderGlowBlur(guiGraphics, partialTick);
    }

    @Override
    public void tick() {
        if(this.minecraft != null) {
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight());
            for (GuiEventListener widget : this.children()) {
                if (widget instanceof TickableWidget tickable) {
                    tickable.tick(mouseX, mouseY);
                }
            }
        }
    }

    protected void renderGlowBlur(GuiGraphics guiGraphics, float partialTick) {
        ChartaClient.processBlurEffect(partialTick);
        RenderTarget glowTarget = ChartaClient.getGlowRenderTarget();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, glowTarget.getColorTextureId());
        ChartaGuiGraphics.innerBlit(guiGraphics, 0, width, 0, height, 0, 1, 1, 0);
    }

    @Override
    public void scheduleTooltip(Component component) {
        this.setTooltipForNextRenderPass(component);
    }

    @Override
    public @Nullable HoverableRenderable getHoverable() {
        return this.hoverable;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}