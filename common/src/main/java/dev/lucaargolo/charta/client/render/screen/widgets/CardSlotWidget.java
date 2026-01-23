package dev.lucaargolo.charta.client.render.screen.widgets;

import dev.lucaargolo.charta.client.render.screen.GameScreen;
import dev.lucaargolo.charta.common.game.api.GameSlot;
import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.game.api.game.Game;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.menu.CardSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CardSlotWidget<G extends Game<G, M>, M extends AbstractCardMenu<G, M>> extends AbstractCardWidget {

    private final GameScreen<G, M> parent;
    private final CardSlot<G, M> cardSlot;

    private final List<CardSlotWidget<G, M>> renderables = new ArrayList<>();
    private boolean renderablesDirty = false;

    private CardSlotWidget<G, M> hoverable = null;

    public CardSlotWidget(GameScreen<G, M> parent, CardSlot<G, M> slot) {
        super(parent, null, null, 0xFFFFFF, slot.x, slot.y, slot.isSmall() ? 0.333f : 1f);
        this.parent = parent;
        this.cardSlot = slot;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GameSlot slot = cardSlot.getSlot();
        if(cardSlot.isExtended()) {
            this.setPreciseWidth(CardSlot.getWidth(cardSlot));
            this.setPreciseHeight(CardSlot.getHeight(cardSlot));
            if(renderables.size() != slot.size() || renderablesDirty) {
                this.hoverable = null;
                renderables.clear();

                float left = 0f, leftOffset = 0f;
                float topOffset = 0f;

                float childWidth = cardSlot.isSmall() ? CardSlot.getWidth(CardSlot.Type.SMALL) : CardSlot.getWidth(CardSlot.Type.DEFAULT);
                float maxLeftOffset = childWidth + childWidth/10f;

                if(cardSlot.getType() != CardSlot.Type.VERTICAL) {
                    leftOffset = childWidth + Math.max(0f, this.getPreciseWidth() - (slot.size() * childWidth) / (float) slot.size());
                    float totalWidth = childWidth + (leftOffset * (slot.size() - 1f));
                    float leftExcess = totalWidth - this.getPreciseWidth();
                    if (leftExcess > 0) {
                        leftOffset -= leftExcess / (slot.size() - 1f);
                    }
                    totalWidth = childWidth + (maxLeftOffset * (slot.size() - 1f));
                    left = 0;
                    if (leftOffset > maxLeftOffset) {
                        left = Math.max(leftOffset - maxLeftOffset, (this.getPreciseWidth() - totalWidth));
                        leftOffset = maxLeftOffset;
                    }
                }else{
                    topOffset = 10f;
                    if(topOffset * (slot.size() - 1) + CardSlot.getHeight(CardSlot.Type.DEFAULT) > this.getPreciseHeight()) {
                        topOffset = (this.getPreciseHeight() - CardSlot.getHeight(CardSlot.Type.DEFAULT)) / (slot.size() - 1);
                    }
                }

                int i = 0;
                for (Card card : slot.getCards()) {
                    int index = i;
                    CardSlot<G, M> childCardSlot = new CardSlot<>(
                        this.parent.getMenu().getGame(),
                        g -> {
                            GameSlot inner = new GameSlot(List.of(card));
                            inner.highlightColor = slot.highlightColor;
                            inner.highlightTime = slot.highlightTime;
                            return inner;
                        },
                        cardSlot.x + leftOffset * i,
                        cardSlot.y + topOffset * i,
                        cardSlot.isSmall() ? CardSlot.Type.SMALL : CardSlot.Type.DEFAULT
                    );
                    CardSlotWidget<G, M> child = new ChildCardSlotWidget(this.parent, childCardSlot, index, Mth.floor(leftOffset), Mth.floor(topOffset));

                    child.setPreciseX(childCardSlot.x + parent.getLeftPos() + left/2f);
                    if(cardSlot.getType() == CardSlot.Type.HORIZONTAL) {
                        child.setPreciseY(childCardSlot.y + parent.height - child.getPreciseHeight());
                    }else if(cardSlot.getType() == CardSlot.Type.PREVIEW) {
                        child.setPreciseY(childCardSlot.y);
                    }else{
                        child.setPreciseY(childCardSlot.y + parent.getTopPos());
                    }

                    renderables.add(child);
                    i++;
                }
                renderablesDirty = false;
            }

            for (CardSlotWidget<G, M> renderable : renderables) {
                if (renderable != this.hoverable || cardSlot.getType() == CardSlot.Type.VERTICAL) {
                    /*
                    This method call might seem weird at first, but by rendering the
                    current hoverable before each other possible hoverable, we stop
                    the cards from flashing weirdly on certain changing cases.
                     */
                    if (this.hoverable != null && cardSlot.getType() != CardSlot.Type.VERTICAL) {
                        this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                    }
                    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                    if (renderable.isHovered && (cardSlot.getType() == CardSlot.Type.VERTICAL || this.hoverable == null || !this.hoverable.isHovered)) {
                        this.hoverable = renderable;
                    }
                }
            }

            if (this.hoverable != null) {
                if(cardSlot.getType() != CardSlot.Type.VERTICAL) {
                    this.hoverable.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                if (!this.hoverable.isHovered) {
                    this.hoverable = null;
                }
            }
            if(slot.highlightTime > 0) {
                CardSlotWidget<G, M> first = this.renderables.getFirst();
                CardSlotWidget<G, M> last = this.renderables.getLast();
                guiGraphics.fill((int) first.getPreciseX() - 2, (int) first.getPreciseY() - 2, (int) (last.getPreciseX()+last.getPreciseWidth()) + 2, (int) (last.getPreciseY()+last.getPreciseHeight()) + 2, 0x66000000 + slot.highlightColor);
            }
        }else{
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            if(!(this instanceof ChildCardSlotWidget) && slot.highlightTime > 0) {
                guiGraphics.fill((int) this.getPreciseX() - 2, (int) this.getPreciseY() - 2, (int) (this.getPreciseX()+this.getPreciseWidth()) + 2, (int) (this.getPreciseY()+this.getPreciseHeight()) + 2, 0x66000000 + slot.highlightColor);
            }
        }
    }

    @Override
    public void tick(int mouseX, int mouseY) {
        super.tick(mouseX, mouseY);
        int i = 0;
        GameSlot slot = cardSlot.getSlot();
        if(slot.highlightTime > 0) {
            slot.highlightTime--;
        }
        for(CardSlotWidget<G, M> renderable : renderables) {
            if(i >= slot.size() || !renderable.cardSlot.getSlot().contains(slot.get(i)))
                renderablesDirty = true;
            if(cardSlot.getType() != CardSlot.Type.VERTICAL || i == renderables.size() - 1)
                renderable.tick(mouseX, mouseY);
            else
                renderable.tick(mouseX, mouseY+20);
            i++;
        }
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@Nullable ResourceLocation cardId, boolean glow) {
        Card card = cardSlot.getSlot().getLast();
        if(card.flipped()) {
            return parent.getDeck().getTexture(glow);
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

    private class ChildCardSlotWidget extends CardSlotWidget<G, M> {

        private final int index;
        private final float leftOffset;
        private final float topOffset;

        public ChildCardSlotWidget(GameScreen<G, M> parent, CardSlot<G, M> slot, int index, float leftOffset, float topOffset) {
            super(parent, slot);
            this.index = index;
            this.leftOffset = leftOffset;
            this.topOffset = topOffset;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            if (this.visible) {
                float actualWidth = this.getPreciseWidth();
                if(CardSlotWidget.this.cardSlot.getType() != CardSlot.Type.VERTICAL && !this.isHovered() && this.index < (CardSlotWidget.this.cardSlot.getSlot().size() - 1)) {
                    actualWidth = this.leftOffset;
                }
                float actualHeight = this.getPreciseHeight();
                if(CardSlotWidget.this.cardSlot.getType() == CardSlot.Type.VERTICAL && !this.isHovered() && this.index < (CardSlotWidget.this.cardSlot.getSlot().size() - 1)) {
                    actualHeight = this.topOffset;
                }
                this.isHovered = guiGraphics.containsPointInScissor(mouseX, mouseY)
                        && mouseX >= this.getX()
                        && mouseY >= this.getY()
                        && mouseX < this.getX() + actualWidth
                        && mouseY < this.getY() + actualHeight;
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
