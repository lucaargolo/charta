package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.gui.screens.CardMenuScreen;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.menu.CardSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CardSlotWidget<G extends CardGame<G>> extends AbstractCardWidget {

    private final CardMenuScreen<G, ?> parent;
    private final CardSlot<G> cardSlot;

    private final List<CardSlotWidget<G>> renderables = new ArrayList<>();
    private boolean renderablesDirty = false;

    private CardSlotWidget<G> hoverable = null;

    public CardSlotWidget(CardMenuScreen<G, ?> parent, CardSlot<G> slot) {
        super(parent, null, slot.x, slot.y, slot.isSmall() ? 0.333f : 1f);
        this.parent = parent;
        this.cardSlot = slot;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(cardSlot.isExtended() && cardSlot.isSmall()) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX()+this.getWidth(), this.getY()+this.getHeight(), 0xFFFF0000);
        }
        List<Card> cards = cardSlot.getCards();
        if(cardSlot.isExtended()) {
            this.setPreciseWidth(CardSlot.getWidth(cardSlot));
            if(renderables.size() != cards.size() || renderablesDirty) {
                renderables.clear();
                int i = 0;
                float childWidth = cardSlot.isSmall() ? CardSlot.getWidth(CardSlot.Type.DEFAULT_SMALL) : CardSlot.getWidth(CardSlot.Type.DEFAULT);
                float maxOffset = childWidth + childWidth/10f;
                float offset = childWidth + Math.max(0f, this.getPreciseWidth() - (cards.size() * childWidth)/(float) cards.size());
                float totalWidth = childWidth + (offset * (cards.size() - 1f));
                float excess = totalWidth - this.getPreciseWidth();
                if(excess > 0) {
                    offset -= excess / (cards.size() - 1f);
                }
                totalWidth = childWidth + (maxOffset * (cards.size() - 1f));
                float left = 0;
                if(offset > maxOffset) {
                    left = Math.max(offset - maxOffset, (this.getPreciseWidth() - totalWidth));
                    offset = maxOffset;
                }
                for (Card card : cards) {
                    int index = i;
                    CardSlot<G> childCardSlot = new CardSlot<>(
                        this.parent.getGame(),
                        game -> List.of(card),
                        cardSlot.x + offset * i,
                        cardSlot.y,
                        cardSlot.isSmall() ? CardSlot.Type.DEFAULT_SMALL : CardSlot.Type.DEFAULT
                    );
                    CardSlotWidget<G> child = new ChildCardSlotWidget(this.parent, childCardSlot, index, Mth.floor(offset));
                    child.setPreciseX(childCardSlot.x + parent.getGuiLeft() + left/2f);
                    child.setPreciseY(childCardSlot.y + parent.getGuiTop());
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
        if(!(this instanceof ChildCardSlotWidget)) {
            guiGraphics.drawString(parent.getMinecraft().font, cards.size() + "", this.getX(), this.getY(), 0x00FF00);
        }
    }

    @Override
    public void tick(int mouseX, int mouseY) {
        super.tick(mouseX, mouseY);
        int i = 0;
        for(CardSlotWidget<G> renderable : renderables) {
            List<Card> cards = cardSlot.getCards();
            if(i >= cards.size() || !renderable.cardSlot.getCards().contains(cards.get(i)))
                renderablesDirty = true;
            renderable.tick(mouseX, mouseY);
            i++;
        }
    }

    @Override
    public ResourceLocation getCardTexture(ResourceLocation cardId) {
        Card card = cardSlot.getCards().getLast();
        if(card.isFlipped()) {
            return parent.getDeck().getDeckTexture();
        }else{
            return parent.getDeck().getCardTexture(card);
        }
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
                if(!this.isHovered() && this.index < (CardSlotWidget.this.cardSlot.getCards().size() - 1)) {
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
