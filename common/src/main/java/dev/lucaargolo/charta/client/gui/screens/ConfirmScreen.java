package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.ChartaMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfirmScreen extends Screen {

    private static final ResourceLocation TEXTURE = ChartaMod.id("textures/gui/confirm.png");

    private final int imageWidth = 175;
    private final int imageHeight = 108;

    private int leftPos;
    private int topPos;

    private final Screen parent;
    private final Component text;
    private final Runnable action;
    private final boolean reversed;

    public ConfirmScreen(Screen parent, Component text, boolean reversed, Runnable action) {
        super(Component.translatable("message.charta.are_you_sure"));
        this.parent = parent;
        this.text = text;
        this.reversed = reversed;
        this.action = action;

    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        assert minecraft != null;
        if ((int) mouseX >= leftPos + 21 && (int) mouseX <= leftPos + 81 && (int) mouseY >= topPos + 75 && (int) mouseY <= topPos + 95) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if(reversed) action.run();
            this.onClose();
            return true;
        } else if ((int) mouseX >= leftPos + 95 && (int) mouseX <= leftPos + 155 && (int) mouseY >= topPos + 75 && (int) mouseY <= topPos + 95) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if(!reversed) action.run();
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.drawCenteredString(font, title, leftPos + imageWidth / 2, topPos + 9, 0xFFFFFF);

        List<FormattedCharSequence> split = font.split(text, 140);
        for (int i = 0; i < split.size(); i++) {
            FormattedCharSequence sequence = split.get(i);
            graphics.drawString(font, sequence, leftPos + 16, topPos + 26 + (10*i), 0xFFFFFF);
        }

        if (mouseX >= leftPos + 21 && mouseX <= leftPos + 81 && mouseY >= topPos + 75 && mouseY <= topPos + 95) {
            graphics.blit(TEXTURE, leftPos + 21, topPos + 75, 0, 108, 60, 20);
        } else if (mouseX >= leftPos + 95 && mouseX <= leftPos + 155 && mouseY >= topPos + 75 && mouseY <= topPos + 95) {
            graphics.blit(TEXTURE, leftPos + 95, topPos + 75, 60, 108, 60, 20);
        }

        graphics.drawCenteredString(font, reversed ? Component.translatable("button.charta.confirm") : Component.translatable("button.charta.cancel"), leftPos + 51, topPos + 80, 0xFFFFFF);
        graphics.drawCenteredString(font, reversed ? Component.translatable("button.charta.cancel") : Component.translatable("button.charta.confirm"), leftPos + 125, topPos + 80, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if(this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
