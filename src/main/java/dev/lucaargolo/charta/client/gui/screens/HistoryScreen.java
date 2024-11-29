package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.client.ChartaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryScreen extends Screen {

    private final Screen parent;
    private HistoryWidget widget;

    public HistoryScreen(Screen parent) {
        super(Component.translatable("charta.game_history"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        widget = this.addRenderableWidget(new HistoryWidget(minecraft, width, height-60, 30));
        ChartaClient.LOCAL_HISTORY.forEach(triple -> widget.addEntry(new Play(triple)));
        widget.setClampedScrollAmount(Double.MAX_VALUE);
        this.addRenderableWidget(new Button.Builder(Component.literal("X"), b -> {
            this.onClose();
        }).bounds(5, 5, 20, 20).tooltip(Tooltip.create(Component.translatable("charta.message.go_back"))).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width/2, 10, 0xFFFFFFFF);
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


    @OnlyIn(Dist.CLIENT)
    public class Play extends ContainerObjectSelectionList.Entry<Play> {

        private final Component player;
        private final int cards;
        private final Component play;

        public Play(Triple<Component, Integer, Component> triple) {
            this.player = triple.getLeft();
            this.cards = triple.getMiddle();
            this.play = triple.getRight();
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.fill(left+101, top - 4, left +102, top + height, 0x33FFFFFF);
            guiGraphics.fill(left+width-108, top - 4, left+width-107, top + height, 0x33FFFFFF);
            if (!player.equals(Component.empty())) {
                guiGraphics.drawString(font, player, left, top, 0xFFFFFFFF);
                guiGraphics.drawString(font, Component.literal(Integer.toString(cards)).append(" ").append(cards > 1 ? Component.translatable("charta.cards") : Component.translatable("charta.card")), left + width - 101, top, 0xFFFFFFFF);
            }
            guiGraphics.drawScrollingString(font, play, left + 108, left+width-108, top, 0xFFFFFFFF);

        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }
    }

    public class HistoryWidget extends ContainerObjectSelectionList<Play> {

        public HistoryWidget(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 15);
        }

        @Override
        public int addEntry(@NotNull Play entry) {
            return super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return Math.min(600, minecraft.getWindow().getGuiScaledWidth()-8);
        }
    }

}
