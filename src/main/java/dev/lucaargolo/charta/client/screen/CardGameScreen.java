package dev.lucaargolo.charta.client.screen;

import com.mojang.blaze3d.vertex.*;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.CardRenderType;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CardGameScreen extends Screen {

    public CardGameScreen() {
        super(Component.empty());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation textureId = ChartaClient.getCardTexture(Charta.id("standard/1_13"));
        RenderType renderType = CardRenderType.getCardType(textureId);

        float xDif = (width/2f - mouseX)/width/2f;
        float yDif = (height/2f - mouseY)/height/2f;



        int cardWidth = CardImage.WIDTH*4;
        int cardHeight = CardImage.HEIGHT*4;
        int x1 = width/2 - cardWidth/2;
        int y1 = height/2 - cardHeight/2;
        int x2 = x1 + cardWidth;
        int y2 = y1 + cardHeight;

        if(mouseX > x1 && mouseX < x2 && mouseY > y1 && mouseY < y2) {
            ChartaClient.CARD_INSET.set(0f);
            ChartaClient.CARD_FOV.set(30f);
            ChartaClient.CARD_X_ROT.set(yDif*90f);
            ChartaClient.CARD_Y_ROT.set(xDif*-90f);
        }else{
            ChartaClient.CARD_INSET.set(30f);
            ChartaClient.CARD_FOV.set(30f);
            ChartaClient.CARD_X_ROT.set(0f);
            ChartaClient.CARD_Y_ROT.set(0f);
        }

        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(renderType);
        PoseStack.Pose pose = guiGraphics.pose().last();
        drawCard(pose, consumer, x1, y1, x2, y2);
    }

    private void drawCard(PoseStack.Pose pose, VertexConsumer consumer, int x1, int y1, int x2, int y2) {
        consumer.addVertex(pose, x1, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x1, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y2, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
        consumer.addVertex(pose, x2, y1, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
