package dev.lucaargolo.charta.game.solitaire;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.charta.client.gui.screens.GameScreen;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.network.RestoreSolitairePayload;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.PacketUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SolitaireScreen extends GameScreen<SolitaireGame, SolitaireMenu> {

    private Component hint;
    private int hintTime = 0;
    private int lastMoves = 0;

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
                if (menu.getGame().getSlot(i + 2).isEmpty()) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(leftPos + 140.5 + i * 42.5, topPos + 22, 0f);
                    guiGraphics.pose().translate(0.5f, 0f, 0f);
                    guiGraphics.pose().scale(1.5f, 1.5f, 1.5f);
                    ChartaGuiGraphics.blitWhiteSuitAndGlow(guiGraphics, this.getDeck(), suit, 0, 0, 0, 0, 13, 13, 13, 13);
                    RenderSystem.defaultBlendFunc();
                    guiGraphics.pose().popPose();
                }
                i++;
            }
        }

        Component text = Component.literal("Solitaire");
        guiGraphics.drawString(font, text, width/2 - font.width(text)/2, 14, 0xFFFFFFFF);

        int x = (width/2 - ((int) CardSlot.getWidth(CardSlot.Type.HORIZONTAL))/2)/2 - 65/2;
        int y = height - 18/2 - 14;
        int color = 0x2d99ff;

        boolean canRestore = menu.canRestore() && menu.getCarriedCards().isEmpty();
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + (canRestore ? color : 0x666666));
        Vec3 c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(WIDGETS, x, y, 59, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        text = Component.literal("Undo");
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+5, 0xFFFFFFFF);
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            guiGraphics.fill(x+1, y+1, x+63, y+16 ,0x33FFFFFF);
            scheduleTooltip(Component.translatable("message.charta.undo"));
        }

        int moves = menu.getMoves();
        if(moves != lastMoves) {
            hintTime = 0;
            hint = null;
            for(GameSlot slot : menu.getGame().getSlots()) {
                slot.highlightTime = 0;
            }
        }
        lastMoves = moves;

        if(hint != null && hintTime > 0) {
            text = hint;
            List<FormattedCharSequence> list = font.split(text, 180);
            if(list.size() == 1) {
                guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, height - 18, 0xFFFFFFFF);
            }else{
                for(i = 0; i < list.size(); i++) {
                    guiGraphics.drawString(font, list.get(i), width / 2 - font.width(list.get(i)) / 2, height - 23 + 10*i, 0xFFFFFFFF);
                }
            }
        }else {
            text = Component.translatable("message.charta.moves", moves);
            guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, height - 23, 0xFFFFFFFF);

            int time = menu.getTime();
            String seconds = String.valueOf(time % 60);
            if (seconds.length() == 1) {
                seconds = "0" + seconds;
            }
            String minutes = String.valueOf(time / 60);
            if (minutes.length() == 1) {
                minutes = "0" + minutes;
            }

            text = Component.literal(minutes + ":" + seconds);
            guiGraphics.drawString(font, text, width / 2 - font.width(text) / 2, height - 13, 0xFFFFFFFF);
        }

        x += width/2 + ((int) CardSlot.getWidth(CardSlot.Type.HORIZONTAL))/2;
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + color);
        c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(WIDGETS, x, y, 59, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        text = Component.literal("Hint");
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+5, 0xFFFFFFFF);
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            guiGraphics.fill(x+1, y+1, x+63, y+16 ,0x33FFFFFF);
            scheduleTooltip(Component.translatable("message.charta.hint"));
        }



    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width/2 - ((int) CardSlot.getWidth(CardSlot.Type.HORIZONTAL))/2)/2 - 65/2;
        int y = height - 18/2 - 14;
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            boolean canRestore = menu.canRestore() && menu.getCarriedCards().isEmpty();
            if(canRestore) {
                PacketUtils.sendToServer(new RestoreSolitairePayload());
                return true;
            }
        }
        x += width/2 + ((int) CardSlot.getWidth(CardSlot.Type.HORIZONTAL))/2;
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            Pair<Component, List<GameSlot>> hint = menu.getGame().getHint();
            this.hint = hint.getFirst();
            this.hintTime = 100;
            for(GameSlot slot : hint.getSecond()) {
                slot.highlightTime = hintTime;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if(this.hintTime > 0) {
            this.hintTime--;
        }else{
            this.hint = null;
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
