package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.client.gui.components.CardSlotWidget;
import dev.lucaargolo.charta.client.gui.components.CardWidget;
import dev.lucaargolo.charta.client.gui.components.DeckWidget;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.network.CardContainerSlotClickPayload;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import dev.lucaargolo.charta.utils.TickableWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CardMenuScreen<G extends CardGame, T extends AbstractCardMenu<G>> extends AbstractContainerScreen<T> implements HoverableRenderable {

    private final List<CardSlotWidget<G>> slotWidgets = new ArrayList<>();
    private HoverableRenderable hoverable = null;
    private CardSlot<G> hoveredCardSlot = null;
    private int hoveredCardId = -1;

    public CardMenuScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        menu.cardSlots.forEach(slot -> {
            slotWidgets.add(new CardSlotWidget<>(this, slot));
        });
    }

    public G getGame() {
        return menu.getGame();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredCardSlot != null) {
            PacketDistributor.sendToServer(new CardContainerSlotClickPayload(menu.containerId, hoveredCardSlot.index, hoveredCardId));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        List<Renderable> renderablesBackup = List.copyOf(this.renderables);

        this.hoveredCardSlot = null;
        for (int k = 0; k < this.menu.cardSlots.size(); k++) {
            CardSlot<G> slot = this.menu.cardSlots.get(k);

            if (this.isHoveringPrecise(slot, mouseX, mouseY)) {
                this.hoveredCardSlot = slot;
            }

            if(!slot.isEmpty()) {
                CardSlotWidget<G> slotWidget = this.slotWidgets.get(k);
                slotWidget.setPreciseX(slot.x + this.leftPos);
                slotWidget.setPreciseY(slot.y + this.topPos);
                this.renderables.add(slotWidget);
            }
        }

        if(this.hoverable != null && !this.renderables.contains(hoverable)) {
            this.hoverable = null;
        }

        for (Renderable renderable : this.renderables) {
            if(renderable != this.hoverable) {
                /*
                This method call might seem weird at first, but by rendering the
                current hoverable before each other possible hoverable, we stop
                the cards from flashing weirdly on certain changing cases.
                 */
                if(this.hoverable != null) {
                    this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                if(renderable instanceof AbstractWidget widget && renderable instanceof HoverableRenderable) {
                    if(widget.isHovered && (!(this.hoverable instanceof AbstractWidget other) || !other.isHovered)){
                        this.hoverable = (HoverableRenderable) renderable;
                    }
                }
            }
        }

        this.renderables.clear();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderables.addAll(renderablesBackup);

        if(this.hoverable != null) {
            this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
            if(this.hoverable instanceof AbstractWidget widget && !widget.isHovered) {
                this.hoverable = null;
            }
        }

        if(this.hoveredCardSlot != null && this.hoverable instanceof CardSlotWidget<?> cardSlotWidget) {
            this.hoveredCardId = cardSlotWidget.getHoveredId();
        }

        List<Card> cards = this.menu.getCarriedCards();
        if (!cards.isEmpty()) {
            Card card = cards.getLast();
            if(card.isFlipped()) {
                DeckWidget.renderDeck(card.getId(), guiGraphics, mouseX-CardImage.WIDTH, mouseY-CardImage.HEIGHT, mouseX, mouseY, partialTick);
            }else{
                CardWidget.renderCard(card.getId(), guiGraphics, mouseX-CardImage.WIDTH, mouseY-CardImage.HEIGHT, mouseX, mouseY, partialTick);
            }
        }
    }

    public boolean isHoveredCardSlot(CardSlot<G> slot) {
        return this.hoveredCardSlot == slot;
    }

    public void setHoveredCardSlot(CardSlot<G> hoveredCardSlot) {
        this.hoveredCardSlot = hoveredCardSlot;
    }

    private boolean isHoveringPrecise(CardSlot<G> slot, float mouseX, float mouseY) {
        return this.isHoveringPrecise(slot.x, slot.y, CardSlot.getWidth(slot), CardSlot.getHeight(slot), mouseX, mouseY);
    }

    protected boolean isHoveringPrecise(float x, float y, float width, float height, double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        mouseX -= i;
        mouseY -= j;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if(this.minecraft != null) {
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight());
            for (GuiEventListener widget : this.children()) {
                if (widget instanceof TickableWidget tickable) {
                    tickable.tick(mouseX, mouseY);
                }
            }
            for(CardSlotWidget<G> widget : this.slotWidgets) {
                widget.tick(mouseX, mouseY);
            }
        }
    }


    @Override
    public @Nullable HoverableRenderable getHoverable() {
        return this.hoverable;
    }

}
