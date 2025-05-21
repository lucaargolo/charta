package dev.lucaargolo.charta.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.Suit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix4f;

import java.util.Deque;

public class ChartaGuiGraphics {

    public static void blitSuitAndGlow(GuiGraphics parent, CardDeck deck, Suit suit, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ResourceLocation textureLocation = deck.getSuitTexture(suit, false);
        ResourceLocation glowLocation = deck.getSuitTexture(suit, true);
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaClient.getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitImageGlow(parent, glowLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void blitWhiteSuitAndGlow(GuiGraphics parent, CardDeck deck, Suit suit, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ResourceLocation textureLocation = deck.getSuitTexture(suit, false);
        ResourceLocation glowLocation = deck.getSuitTexture(suit, true);
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaClient.getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitWhiteImageGlow(parent, glowLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void blitImage(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getImageShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitWhiteImage(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getWhiteImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getWhiteImageShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitImageGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getImageGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitWhiteImageGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getWhiteImageArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getWhiteImageGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, (u + 0.0F) / textureWidth, (u + width) / textureWidth, (v + 0.0F) / textureHeight, (v + height) / textureHeight);
    }

    public static void blitCard(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getCardArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getCardShader);
        }
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitCardGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        if(IrisCompat.isPresent()) {
            RenderSystem.setShader(ChartaClient::getCardArgbShader);
        }else{
            RenderSystem.setShader(ChartaClient::getCardGlowShader);
        }
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitPerspective(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(ChartaClient::getPerspectiveShader);
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void blitGrayscale(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(ChartaClient::getGrayscaleShader);
        innerBlit(parent, x, x + width, y, y + height, 0f, 1f, 0f, 1f);
    }

    public static void innerBlit(GuiGraphics parent, float x1, float x2, float y1, float y2, float minU, float maxU, float minV, float maxV) {
        RenderSystem.enableBlend();
        Matrix4f matrix4f = parent.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix4f, x1, y1, 0).uv(minU, minV).color(1f, 1f, 1f, 1f).endVertex();
        bufferbuilder.vertex(matrix4f, x1, y2, 0).uv(minU, maxV).color(1f, 1f, 1f, 1f).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y2, 0).uv(maxU, maxV).color(1f, 1f, 1f, 1f).endVertex();
        bufferbuilder.vertex(matrix4f, x2, y1, 0).uv(maxU, minV).color(1f, 1f, 1f, 1f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }

    public static void renderBackgroundBlur(Screen screen, GuiGraphics guiGraphics, float partialTick) {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget blurTarget = ChartaClient.getBlurRenderTarget();
        //Draw main target into blur target
        blurTarget.bindWrite(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, mainTarget.getColorTextureId());
        ChartaGuiGraphics.innerBlit(guiGraphics, 0, screen.width, 0, screen.height, 0, 1, 1, 0);
        //Blur main target
        ChartaClient.processBlurEffect(partialTick);
        //Draw blur target into main target
        mainTarget.bindWrite(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, blurTarget.getColorTextureId());
        ChartaGuiGraphics.innerBlit(guiGraphics, 0, screen.width, 0, screen.height, 0, 1, 1, 0);
        //Clear blur target
        blurTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        blurTarget.clear(Minecraft.ON_OSX);
        mainTarget.bindWrite(false);
    }

    public static boolean containsPointInScissor(GuiGraphics graphics, int x, int y) {
        Deque<ScreenRectangle> stack = graphics.scissorStack.stack;
        if(stack.isEmpty()) {
            return true;
        }else{
            ScreenRectangle rect = stack.peek();
            return x >= rect.left() && x < rect.right() && y >= rect.top() && y < rect.bottom();
        }
    }

    public static int getDyeColor(DyeColor color) {
        float[] rgb = color.getTextureDiffuseColors();
        int r = (int) (rgb[0] * 255);
        int g = (int) (rgb[1] * 255);
        int b = (int) (rgb[2] * 255);
        return (r << 16) | (g << 8) | b;
    }
}
