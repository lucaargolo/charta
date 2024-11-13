package dev.lucaargolo.charta.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PaletteGuiGraphics {

    public static void blit(GuiGraphics parent, ResourceLocation textureLocation, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        innerBlit(parent, textureLocation, x, x + width, y, y + height, 0,
            (u + 0.0F) / (float)textureWidth,
            (u + (float)width) / (float)textureWidth,
            (v + 0.0F) / (float)textureHeight,
            (v + (float)height) / (float)textureHeight,
        1f, 1f, 1f, 1f);
    }

    private static void innerBlit(GuiGraphics parent, ResourceLocation textureLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha) {
        RenderSystem.setShaderTexture(0, textureLocation);
        RenderSystem.setShader(ChartaClient::getPositionPaletteColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = parent.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset)
                .setUv(minU, minV)
                .setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset)
                .setUv(minU, maxV)
                .setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset)
                .setUv(maxU, maxV)
                .setColor(red, green, blue, alpha);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset)
                .setUv(maxU, minV)
                .setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

}
