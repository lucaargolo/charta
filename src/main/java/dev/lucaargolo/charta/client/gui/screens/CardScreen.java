package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.components.*;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardScreen extends Screen implements HoverableRenderable {

    private HoverableRenderable hoverable = null;

    public CardScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        for (int x = 1; x < 14; x++) {
            this.addRenderableWidget(new CardWidget(this, Charta.id("standard/1_"+x), (x-1)*25, 0, 1f));
            this.addRenderableWidget(new CardWidget(this, Charta.id("standard/2_"+x), (x-1)*25, 53, 1f));
            this.addRenderableWidget(new CardWidget(this, Charta.id("standard/3_"+x), (x-1)*25, 106, 1f));
            this.addRenderableWidget(new CardWidget(this, Charta.id("standard/4_"+x), (x-1)*25, 159, 1f));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
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


    @Override
    public @Nullable HoverableRenderable getHoverable() {
        return this.hoverable;
    }

}
