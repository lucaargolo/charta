package dev.lucaargolo.hexedaces.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lucaargolo.hexedaces.HexedAces;
import dev.lucaargolo.hexedaces.client.HexedAcesClient;
import dev.lucaargolo.hexedaces.utils.CardImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CardGameScreen extends Screen {

    public CardGameScreen() {
        super(Component.empty());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation textureId = HexedAcesClient.getCardTexture(HexedAces.id("standard/2_2"));
        guiGraphics.blit(textureId, 0, 0, 0, 0, CardImage.WIDTH, CardImage.HEIGHT, CardImage.WIDTH, CardImage.HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
