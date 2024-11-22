package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.CrazyEightsGame;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import net.minecraft.ChatFormatting;
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
        this.imageHeight = 170;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component text;

        Suit suit = menu.getCurrentSuit();
        if(suit != null) {
            text = Component.translatable("charta.suit");
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 40, 0xFFFFFFFF);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(imageWidth / 2f - 10f, 50f, 0f);
            guiGraphics.pose().translate(0.5f, 0f, 0f);
            guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);
            ChartaGuiGraphics.blitImageAndGlow(guiGraphics, this.getDeck().getSuitTexture(suit), 0, 0, 0, 0, 13, 13, 13, 13);
            guiGraphics.pose().popPose();
        }

        CardPlayer player = menu.getCurrentPlayer();
        DyeColor color = player.getColor();
        if(menu.isGameReady()) {
            if (menu.isCurrentPlayer()) {
                text = Component.translatable("charta.message.your_turn").withStyle(s -> s.withColor(color.getTextureDiffuseColor()));
            } else {
                text = Component.translatable("charta.message.other_turn", player.getName()).withStyle(s -> s.withColor(color.getTextureDiffuseColor()));
            }
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 105, 0xFFFFFFFF);
            text = Component.translatable("charta.message.draws_left", menu.getDrawsLeft());
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 115, 0xFFFFFFFF);
        }else{
            text = Component.translatable("charta.message.dealing_cards").withStyle(ChatFormatting.GOLD);
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 110, 0xFFFFFFFF);
        }

    }
}
