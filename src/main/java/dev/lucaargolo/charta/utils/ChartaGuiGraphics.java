package dev.lucaargolo.charta.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.compat.IrisCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ChartaGuiGraphics {

    public static void blitImageAndGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaClient.getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 0f);
        ChartaGuiGraphics.blitImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitImageGlow(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void blitWhiteImageAndGlow(GuiGraphics parent, ResourceLocation textureLocation, float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        ChartaClient.getGlowRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(0f, 0f, 0f, 0f);
        ChartaGuiGraphics.blitWhiteImage(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ChartaGuiGraphics.blitWhiteImageGlow(parent, textureLocation, x, y, u, v, width, height, textureWidth, textureHeight);
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
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, x1, y1, 0).setUv(minU, minV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x1, y2, 0).setUv(minU, maxV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x2, y2, 0).setUv(maxU, maxV).setColor(1f, 1f, 1f, 1f);
        bufferbuilder.addVertex(matrix4f, x2, y1, 0).setUv(maxU, minV).setColor(1f, 1f, 1f, 1f);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

}
