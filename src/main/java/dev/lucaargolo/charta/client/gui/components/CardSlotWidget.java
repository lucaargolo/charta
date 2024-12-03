package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.gui.screens.CardMenuScreen;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.CardSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CardSlotWidget<G extends CardGame<G>> extends AbstractCardWidget {

    private final CardMenuScreen<G, ?> parent;
    private final CardSlot<G> cardSlot;

    private final List<CardSlotWidget<G>> renderables = new ArrayList<>();
    private boolean renderablesDirty = false;

    private CardSlotWidget<G> hoverable = null;

    public CardSlotWidget(CardMenuScreen<G, ?> parent, CardSlot<G> slot) {
        super(parent, null, null, 0xFFFFFF, slot.x, slot.y, slot.isSmall() ? 0.333f : 1f);
        this.parent = parent;
        this.cardSlot = slot;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GameSlot slot = cardSlot.getSlot();
        if(cardSlot.isExtended()) {
            this.setPreciseWidth(CardSlot.getWidth(cardSlot));
            if(renderables.size() != slot.size() || renderablesDirty) {
                this.hoverable = null;
                renderables.clear();
                int i = 0;
                float childWidth = cardSlot.isSmall() ? CardSlot.getWidth(CardSlot.Type.SMALL) : CardSlot.getWidth(CardSlot.Type.DEFAULT);
                float maxOffset = childWidth + childWidth/10f;
                float offset = childWidth + Math.max(0f, this.getPreciseWidth() - (slot.size() * childWidth)/(float) slot.size());
                float totalWidth = childWidth + (offset * (slot.size() - 1f));
                float excess = totalWidth - this.getPreciseWidth();
                if(excess > 0) {
                    offset -= excess / (slot.size() - 1f);
                }
                totalWidth = childWidth + (maxOffset * (slot.size() - 1f));
                float left = 0;
                if(offset > maxOffset) {
                    left = Math.max(offset - maxOffset, (this.getPreciseWidth() - totalWidth));
                    offset = maxOffset;
                }
                for (Card card : slot.getCards()) {
                    int index = i;
                    CardSlot<G> childCardSlot = new CardSlot<>(
                        this.parent.getMenu().getGame(),
                        g -> new GameSlot(List.of(card)),
                        cardSlot.x + offset * i,
                        cardSlot.y,
                        cardSlot.isSmall() ? CardSlot.Type.SMALL : CardSlot.Type.DEFAULT
                    );
                    CardSlotWidget<G> child = new ChildCardSlotWidget(this.parent, childCardSlot, index, Mth.floor(offset));
                    child.setPreciseX(childCardSlot.x + parent.getGuiLeft() + left/2f);
                    if(cardSlot.getType() == CardSlot.Type.INVENTORY) {
                        child.setPreciseY(childCardSlot.y + parent.height - child.getPreciseHeight());
                    }else if(cardSlot.getType() == CardSlot.Type.PREVIEW) {
                        child.setPreciseY(childCardSlot.y);
                    }else{
                        child.setPreciseY(childCardSlot.y + parent.getGuiTop());
                    }
                    renderables.add(child);
                    i++;
                }
                renderablesDirty = false;
            }
            for (CardSlotWidget<G> renderable : renderables) {
                if (renderable != this.hoverable) {
                    /*
                    This method call might seem weird at first, but by rendering the
                    current hoverable before each other possible hoverable, we stop
                    the cards from flashing weirdly on certain changing cases.
                     */
                    if (this.hoverable != null) {
                        this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                    }
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                    if (renderable.isHovered && (this.hoverable == null || !this.hoverable.isHovered)) {
                        this.hoverable = renderable;
                    }
                }
            }

            if (this.hoverable != null) {
                this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                if (!this.hoverable.isHovered) {
                    this.hoverable = null;
                }
            }
        }else{
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void tick(int mouseX, int mouseY) {
        super.tick(mouseX, mouseY);
        int i = 0;
        for(CardSlotWidget<G> renderable : renderables) {
            GameSlot slot = cardSlot.getSlot();
            if(i >= slot.size() || !renderable.cardSlot.getSlot().contains(slot.get(i)))
                renderablesDirty = true;
            renderable.tick(mouseX, mouseY);
            i++;
        }
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@Nullable ResourceLocation cardId, boolean glow) {
        Card card = cardSlot.getSlot().getLast();
        if(card.isFlipped()) {
            return parent.getDeck().getDeckTexture(glow);
        }else{
            return parent.getDeck().getCardTexture(card, glow);
        }
    }

    @Override
    public String getCardTranslatableKey() {
        Card card = cardSlot.getSlot().getLast();
        return parent.getDeck().getCardTranslatableKey(card);
    }

    @Override
    public int getCardColor() {
        Card card = cardSlot.getSlot().getLast();
        return parent.getDeck().getCardColor(card);
    }

    @Override
    public boolean isHovered() {
        return parent.isHoveredCardSlot(cardSlot);
    }

    public int getHoveredId() {
        return this.hoverable != null ? this.hoverable.getHoveredId() : -1;
    }

    private class ChildCardSlotWidget extends CardSlotWidget<G> {

        private final int index;
        private final float offset;

        public ChildCardSlotWidget(CardMenuScreen<G, ?> parent, CardSlot<G> slot, int index, float offset) {
            super(parent, slot);
            this.index = index;
            this.offset = offset;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            if (this.visible) {
                float actualWidth = this.getPreciseWidth();
                if(!this.isHovered() && this.index < (CardSlotWidget.this.cardSlot.getSlot().size() - 1)) {
                    actualWidth = this.offset;
                }
                this.isHovered = guiGraphics.containsPointInScissor(mouseX, mouseY)
                        && mouseX >= this.getX()
                        && mouseY >= this.getY()
                        && mouseX < this.getX() + actualWidth
                        && mouseY < this.getY() + this.height;
            }
        }

        @Override
        public int getHoveredId() {
            return index;
        }

        @Override
        public boolean isHovered() {
            return CardSlotWidget.this.hoverable == this;
        }
    }

}
