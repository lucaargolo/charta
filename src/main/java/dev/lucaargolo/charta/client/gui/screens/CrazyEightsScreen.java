package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.game.CrazyEightsGame;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CrazyEightsScreen extends CardMenuScreen<CrazyEightsGame, CrazyEightsMenu> {

    public CrazyEightsScreen(CrazyEightsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

}
