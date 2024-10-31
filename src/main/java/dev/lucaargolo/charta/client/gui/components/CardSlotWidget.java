package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.client.gui.screens.CardMenuScreen;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CardSlotWidget<G extends CardGame> extends AbstractCardWidget {

    private final CardMenuScreen<G, ?> parent;
    private final CardSlot<G> cardSlot;

    private final List<CardSlotWidget<G>> renderables = new ArrayList<>();
    private boolean renderablesDirty = false;

    private CardSlotWidget<G> hoverable = null;

    public CardSlotWidget(CardMenuScreen<G, ?> parent, CardSlot<G> slot) {
        super(parent, null, slot.x, slot.y, 1f);
        this.parent = parent;
        this.cardSlot = slot;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<Card> cards = cardSlot.getCards();
        if(cardSlot.isExtended()) {
            if(cards.size() > 1) {
                this.setWidth((int) (CardImage.WIDTH * 1.5f) + (25 * (cards.size() - 1)));
                if(renderables.size() != cards.size() || renderablesDirty) {
                    renderables.clear();
                    int i = 0;
                    for (Card card : cards) {
                        int index = i;
                        CardSlot<G> childCardSlot = new CardSlot<>(this.parent.getGame(), game -> List.of(card), cardSlot.x + (25 * i), cardSlot.y, true);
                        CardSlotWidget<G> child = new CardSlotWidget<>(this.parent, childCardSlot) {
                            @Override
                            public int getHoveredId() {
                                return index;
                            }

                            @Override
                            public boolean isHovered() {
                                return CardSlotWidget.this.hoverable == this;
                            }
                        };
                        child.setX(childCardSlot.x + parent.getGuiLeft());
                        child.setY(childCardSlot.y + parent.getGuiTop());
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
                this.hoverable = null;
                this.setWidth((int) (CardImage.WIDTH * 1.5f));
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
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
        return card.isFlipped() ? ChartaClient.getDeckTexture(card.getId()) : ChartaClient.getCardTexture(card.getId());
    }

    @Override
    public boolean isHovered() {
        return parent.isHoveredCardSlot(cardSlot);
    }

    public int getHoveredId() {
        return this.hoverable != null ? this.hoverable.getHoveredId() : -1;
    }
}
