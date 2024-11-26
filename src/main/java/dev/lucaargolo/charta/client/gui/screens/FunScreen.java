package dev.lucaargolo.charta.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.FunGame;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.FunMenu;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class FunScreen extends CardMenuScreen<FunGame, FunMenu> {

    private static final ResourceLocation TEXTURE = Charta.id("textures/gui/fun.png");

    public FunScreen(FunMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 140;
        this.imageHeight = 180;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        Style style = Style.EMPTY.withFont(Charta.id("minercraftory"));
        int x = (width/2 - ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2)/2 - 65/2;
        int y = height - ((int) CardSlot.getHeight(CardSlot.Type.INVENTORY))/2 - 14;
        int color = menu.getCurrentPlayer().getHand().size() == 1 ? menu.isCurrentPlayer() ? 0x00FF00 : 0xFF0000 : 0x333333;
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + color);
        Vec3 c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(TEXTURE, x, y, 161, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        Component text = Component.literal("Last!").withStyle(style);
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+7, 0xFFFFFFFF);

        x += width/2 + ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2;
        color = menu.isCurrentPlayer() && menu.getDrawStack() > 0 ? Color.HSBtoRGB(0.333f + ((menu.getDrawStack()/32f)*0.666f), 1f, 1f) : 0x333333;
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + color);
        c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(TEXTURE, x, y, 161, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        text = Component.literal(menu.getDrawStack() > 0 ? "Draw "+menu.getDrawStack() : "Draw").withStyle(style);
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+7, 0xFFFFFFFF);

    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Style style = Style.EMPTY.withFont(Charta.id("minercraftory"));

        Component text = Component.literal("Fun").withStyle(style);
        guiGraphics.drawString(font, text, imageWidth/2 - font.width(text)/2, 16, 0xFFFFFFFF);
        text = Component.literal("Draw").withStyle(style);
        guiGraphics.drawString(font, text, imageWidth/4 + 2 - font.width(text)/2, 92, 0xFFFFFFFF);
        text = Component.literal("Play").withStyle(style);
        guiGraphics.drawString(font, text, (3*imageWidth)/4 - 2 - font.width(text)/2, 92, 0xFFFFFFFF);

        FunGame game = this.getGame();
        int index = game.getPlayers().indexOf(menu.getCurrentPlayer());
        if(menu.isReversed()) {
            index--;
            if(index < 0) {
                index = game.getPlayers().size() - 1;
            }
        }else{
            index++;
            if(index > game.getPlayers().size() - 1) {
                index = 0;
            }
        }
        CardPlayer nextPlayer = game.getPlayers().get(index);
        int color = nextPlayer.getColor().getTextureDiffuseColor();
        text = Component.translatable("charta.message.next_player", nextPlayer.getName()).withStyle(s -> s.withColor(nextPlayer.getColor().getTextureDiffuseColor()));
        guiGraphics.drawString(font, text, imageWidth/2 - font.width(text)/2, 132, 0xFFFFFFFF);
        Vec3 c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(TEXTURE, imageWidth/2 - 11, 120, 140, menu.isReversed() ? 12 : 0, 21, 12);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

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
        if(menu.isGameReady()) {
            if (menu.isCurrentPlayer()) {
                text = Component.translatable("charta.message.your_turn").withStyle(s -> s.withColor(player.getColor().getTextureDiffuseColor()));
            } else {
                text = Component.translatable("charta.message.other_turn", player.getName()).withStyle(s -> s.withColor(player.getColor().getTextureDiffuseColor()));
            }
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 110, 0xFFFFFFFF);
        }else{
            text = Component.translatable("charta.message.dealing_cards").withStyle(ChatFormatting.GOLD);
            guiGraphics.drawString(font, text, imageWidth / 2 - font.width(text) / 2, 110, 0xFFFFFFFF);
        }

    }
}
