package dev.lucaargolo.charta.game.fun;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.screens.CardMenuScreen;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.network.CardContainerSlotClickPayload;
import dev.lucaargolo.charta.network.LastFunPayload;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

public class FunScreen extends CardMenuScreen<FunGame, FunMenu> {

    private static final ResourceLocation TEXTURE = Charta.id("textures/gui/fun.png");

    private int lastCooldown = FunGame.LAST_COOLDOWN;
    private boolean drawAll = false;

    @Nullable
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;

    private final Random random = new Random();

    public FunScreen(FunMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 140;
        this.imageHeight = 180;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if(!menu.canDoLast()) {
            lastCooldown = FunGame.LAST_COOLDOWN;
        }else if(menu.didntSayLast()){
            lastCooldown = 0;
        }else if(lastCooldown > 0) {
            lastCooldown--;
        }
        if(!menu.isCurrentPlayer()) {
            drawAll = false;
        }else if(drawAll) {
            if(menu.getCarriedCards().isEmpty()) {
                PacketDistributor.sendToServer(new CardContainerSlotClickPayload(menu.containerId, menu.cardSlots.size()-3, -1));
            }else{
                drawAll = false;
            }
            PacketDistributor.sendToServer(new CardContainerSlotClickPayload(menu.containerId, menu.cardSlots.size()-1, -1));
        }
        if (this.itemActivationTicks > 0) {
            this.itemActivationTicks--;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int x = (width/2 - ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2)/2 - 65/2;
        int y = height - ((int) CardSlot.getHeight(CardSlot.Type.INVENTORY))/2 - 14;
        int color = menu.canDoLast() ? menu.didntSayLast() ? 0x00FF00 : 0xFF0000 : 0x333333;
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + color);
        Vec3 c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(TEXTURE, x, y, 161, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        Component text = Component.literal(menu.canDoLast() && lastCooldown > 0 ? Integer.toString(lastCooldown) : "Last!").withStyle(Charta.MINERCRAFTORY);
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+7, 0xFFFFFFFF);
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            guiGraphics.fill(x+1, y+1, x+63, y+16 ,0x33FFFFFF);
            scheduleTooltip(Component.translatable("charta.message.say_last"));
        }

        x += width/2 + ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2;
        color = menu.isCurrentPlayer() && menu.getDrawStack() > 0 && menu.canDraw() ? Color.HSBtoRGB(0.333f + ((menu.getDrawStack()/32f)*0.666f), 1f, 1f) : 0x333333;
        guiGraphics.fill(x+1, y+1, x+63, y+16, 0xFF000000 + color);
        c = Vec3.fromRGB24(color);
        RenderSystem.setShaderColor((float) c.x, (float) c.y, (float) c.z, 1f);
        guiGraphics.blit(TEXTURE, x, y, 161, 0, 65, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        text = Component.literal(menu.getDrawStack() > 0 ? "Draw "+menu.getDrawStack() : "Draw").withStyle(Charta.MINERCRAFTORY);
        guiGraphics.drawString(font, text, x + 65/2 - font.width(text)/2, y+7, 0xFFFFFFFF);
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            guiGraphics.fill(x+1, y+1, x+63, y+16 ,0x33FFFFFF);
            scheduleTooltip(Component.translatable("charta.message.draw_all_cards"));
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width/2 - ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2)/2 - 65/2;
        int y = height - ((int) CardSlot.getHeight(CardSlot.Type.INVENTORY))/2 - 14;
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            PacketDistributor.sendToServer(new LastFunPayload());
            return true;
        }
        x += width/2 + ((int) CardSlot.getWidth(CardSlot.Type.INVENTORY))/2;
        if(mouseX >= x && mouseX < x+65 && mouseY >= y && mouseY < y+18) {
            drawAll = menu.isCurrentPlayer() && menu.getCarriedCards().isEmpty();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderItemActivationAnimation(guiGraphics, partialTick);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component text = Component.literal("Fun").withStyle(Charta.MINERCRAFTORY);
        guiGraphics.drawString(font, text, imageWidth/2 - font.width(text)/2, 16, 0xFFFFFFFF);
        text = Component.literal("Draw").withStyle(Charta.MINERCRAFTORY);
        guiGraphics.drawString(font, text, imageWidth/4 + 2 - font.width(text)/2, 92, 0xFFFFFFFF);
        text = Component.literal("Play").withStyle(Charta.MINERCRAFTORY);
        guiGraphics.drawString(font, text, (3*imageWidth)/4 - 2 - font.width(text)/2, 92, 0xFFFFFFFF);

        FunGame game = this.menu.getGame();
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

    public void displayItemActivation(ItemStack stack) {
        this.itemActivationItem = stack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
        this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
    }

    @SuppressWarnings("deprecation")
    public void renderItemActivationAnimation(GuiGraphics guiGraphics, float partialTick) {
        if (this.minecraft != null && this.itemActivationItem != null && this.itemActivationTicks > 0) {
            int i = 40 - this.itemActivationTicks;
            float f = ((float)i + partialTick) / 40.0F;
            float f1 = f * f;
            float f2 = f * f1;
            float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
            float f4 = f3 * (float) Math.PI;
            float f5 = this.itemActivationOffX * (float)(guiGraphics.guiWidth() / 4);
            float f6 = this.itemActivationOffY * (float)(guiGraphics.guiHeight() / 4);
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            posestack.translate(
                    (float)(guiGraphics.guiWidth() / 2) + f5 * Mth.abs(Mth.sin(f4 * 2.0F)),
                    (float)(guiGraphics.guiHeight() / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F)),
                    500.0F
            );
            float f7 = 50.0F + 175.0F * Mth.sin(f4);
            posestack.scale(f7, -f7, f7);
            posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f4))));
            posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            guiGraphics.drawManaged(
                () -> this.minecraft
                    .getItemRenderer()
                    .renderStatic(
                        this.itemActivationItem,
                        ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        posestack,
                        guiGraphics.bufferSource(),
                        this.minecraft.level,
                        0
                    )
            );
            posestack.popPose();
        }
    }

}
