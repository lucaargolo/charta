package dev.lucaargolo.charta.game.solitaire;

import dev.lucaargolo.charta.client.gui.screens.CardMenuScreen;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SolitaireScreen extends CardMenuScreen<SolitaireGame, SolitaireMenu> {

    public SolitaireScreen(SolitaireMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = (int) (5 + (CardImage.WIDTH * 1.5f + 5)*7);
        this.imageHeight = 180;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
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
