package dev.lucaargolo.charta.common.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.client.compat.IrisCompat;
import dev.lucaargolo.charta.common.game.api.card.Deck;
import dev.lucaargolo.charta.common.game.api.card.Suit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ChartaGuiGraphics {

    public static void blitSuitAndGlow(GuiGraphics parent, Deck deck, Suit suit, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ResourceLocation textureLocation = deck.getSuitTexture(suit, false);
        ResourceLocation glowLocation = deck.getSuitTexture(suit, true);
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaModClient.getShaderManager().getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitImageGlow(parent, glowLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void blitWhiteSuitAndGlow(GuiGraphics parent, Deck deck, Suit suit, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ResourceLocation textureLocation = deck.getSuitTexture(suit, false);
        ResourceLocation glowLocation = deck.getSuitTexture(suit, true);
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaModClient.getShaderManager().getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitWhiteImageGlow(parent, glowLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void blitImage(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getImageShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitWhiteImage(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getWhiteImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getWhiteImageShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitImageGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getImageGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitWhiteImageGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getWhiteImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getWhiteImageGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitCard(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getCardArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getCardShader);
        }
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitCardGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getCardArgbShader);
        }else{
            RenderSystem.setShader(ChartaModClient.getShaderManager()::getCardGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitPerspective(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(ChartaModClient.getShaderManager()::getPerspectiveShader);
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitGrayscale(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(ChartaModClient.getShaderManager()::getGrayscaleShader);
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void innerBlit(GuiGraphics parent, float x1, float x2, float y1, float y2, float minU, float maxU, float minV, float maxV) {
        RenderSystem.enableBlend();
        Matrix4f matrix4f = parent.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, x1, y1, 0).setUv(minU, minV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x1, y2, 0).setUv(minU, maxV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x2, y2, 0).setUv(maxU, maxV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x2, y1, 0).setUv(maxU, minV).setColor(1f, 1f, 1f, 1f);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawScrollingString(GuiGraphics parent, Font font, Component text, int minX, int maxX, int y, int color) {
        int maxWidth = maxX - minX;
        int textWidth = font.width(text.getVisualOrderText());
        if (textWidth <= maxWidth) {
            parent.drawString(font, text, minX, y, color);
        } else {
            AbstractWidget.renderScrollingString(parent, font, text, minX, y, maxX, y + 9, color);
        }
    }
}
