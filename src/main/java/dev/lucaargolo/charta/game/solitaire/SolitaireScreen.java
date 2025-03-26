package dev.lucaargolo.charta.game.solitaire;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lucaargolo.charta.client.gui.screens.GameScreen;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SolitaireScreen extends GameScreen<SolitaireGame, SolitaireMenu> {

    public SolitaireScreen(SolitaireMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = (int) (5 + (CardImage.WIDTH * 1.5f + 5)*7);
        this.imageHeight = 180;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = 0;
        for(Suit suit : Suit.values()) {
            if(suit != Suit.BLANK) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(leftPos+140.5 + i*42.5, topPos+22, 0f);
                guiGraphics.pose().translate(0.5f, 0f, 0f);
                guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);
                ChartaGuiGraphics.blitWhiteSuitAndGlow(guiGraphics, this.getDeck(), suit, 0, 0, 0, 0, 13, 13, 13, 13);
                RenderSystem.defaultBlendFunc();
                guiGraphics.pose().popPose();
                i++;
            }
        }
    }

    @Override
    public void renderTopBar(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, width, 28, 0x88000000);
    }

    @Override
    public void renderBottomBar(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fill(0, height-28, width, height, 0x88000000);
    }



}
