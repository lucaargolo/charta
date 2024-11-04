package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CrazyEightsGame;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CrazyEightsScreen extends CardMenuScreen<CrazyEightsGame, CrazyEightsMenu> {

    private static final ResourceLocation TEXTURE = Charta.id("textures/gui/crazy_eights.png");

    public CrazyEightsScreen(CrazyEightsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CardGame<CrazyEightsGame> game = this.menu.getGame();
        int players = game.getPlayers().size();
        float totalWidth = CardSlot.getWidth(CardSlot.Type.EXTENDED_SMALL);
        float playersWidth = (players * totalWidth) + ((players-1f) * (totalWidth/10f));
        for(int i = 0; i < players; i++) {
            guiGraphics.drawString(font, "Player "+(i+1), 176/2f - playersWidth/2f + (i*(totalWidth + totalWidth/10f)), -27f, menu.getCurrentPlayer() == i ? 0xFF00FF00 : 0xFFFF0000, false);
        }
        if(menu.isCurrentPlayer()) {
            guiGraphics.drawString(font, "It's your turn!", 0, 0, 0xFF00FF00);
        }else{
            guiGraphics.drawString(font, "It's player "+menu.getCurrentPlayer()+" turn", 0, 0, 0xFF00FF00);
        }
        guiGraphics.drawString(font, "Draws left: "+menu.getDrawsLeft(), 0, 10, 0xFF00FF00);

    }
}
