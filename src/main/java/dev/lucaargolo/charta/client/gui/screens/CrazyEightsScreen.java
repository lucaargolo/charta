package dev.lucaargolo.charta.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CrazyEightsGame;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
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
        guiGraphics.fill(0, 0, width, 27, 0x88000000);
        guiGraphics.fill(0, height-62, width, height, 0x88000000);

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        CardGame<CrazyEightsGame> game = this.menu.getGame();
        int players = game.getPlayers().size();
        float totalWidth = CardSlot.getWidth(CardSlot.Type.PREVIEW);
        float playersWidth = (players * totalWidth) + ((players-1f) * (totalWidth/10f));
        Component text;
        for(int i = 0; i < players; i++) {
            text = Component.literal("Player "+(i+1));
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(width/2f - playersWidth/2f + (i*(totalWidth + totalWidth/10f)) + totalWidth/2f - font.width(text)/4f, 2f, 0f);
            guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
            guiGraphics.drawString(font, text, 0, 0, 0xFFFFFFFF, true);
            guiGraphics.pose().popPose();
        }

        if(menu.isCurrentPlayer()) {
            text = Component.literal("It's your turn!").withStyle(ChatFormatting.YELLOW);
        }else{
            text = Component.literal("It's Player's "+(menu.getCurrentPlayer()+1)+" turn");
        }
        guiGraphics.drawString(font, text, width/2 - font.width(text)/2, topPos+110, 0xFFFFFFFF);
        text = Component.literal("Draws left: "+menu.getDrawsLeft());
        guiGraphics.drawString(font, text, width/2 - font.width(text)/2, topPos+120, 0xFFFFFFFF);
    }
}
