package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.CrazyEightsGame;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class CrazyEightsScreen extends CardMenuScreen<CrazyEightsGame, CrazyEightsMenu> {

    private static final ResourceLocation TEXTURE = Charta.id("textures/gui/crazy_eights.png");

    public CrazyEightsScreen(CrazyEightsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 140;
        this.imageHeight = 175;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component text;
        CardPlayer player = menu.getCurrentPlayer();
        DyeColor color = player.getColor();
        if(menu.isCurrentPlayer()) {
            text = Component.literal("It's your turn!").withStyle(s -> s.withColor(color.getTextureDiffuseColor()));
        }else{
            text = Component.literal("It's ").append(player.getName()).append("'s turn").withStyle(s -> s.withColor(color.getTextureDiffuseColor()));;
        }
        guiGraphics.drawString(font, text, imageWidth/2 - font.width(text)/2, 110, 0xFFFFFFFF);
        text = Component.literal("Draws left: "+menu.getDrawsLeft());
        guiGraphics.drawString(font, text, imageWidth/2 - font.width(text)/2, 120, 0xFFFFFFFF);
    }
}
