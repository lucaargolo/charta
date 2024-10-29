package dev.lucaargolo.charta.client.screen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CardGameScreen extends Screen {

    public CardGameScreen() {
        super(Component.empty());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation textureId = ChartaClient.getCardTexture(Charta.id("standard/2_2"));
        guiGraphics.blit(textureId, 0, 0, 0, 0, CardImage.WIDTH, CardImage.HEIGHT, CardImage.WIDTH, CardImage.HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
