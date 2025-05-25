package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.gui.components.CardWidget;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeckScreen extends CardScreen implements HoverableRenderable {

    private final Screen parent;

    private final CardDeck deck;

    private int headerOffset = 10;
    private int footerOffset = 10;

    public DeckScreen(@Nullable Screen parent, CardDeck deck) {
        super(deck.getName());
        this.parent = parent;
        this.deck = deck;
    }

    @Override
    protected void init() {
        if(parent != null) {
            Component back = Component.literal("\ue5c4").withStyle(Charta.SYMBOLS);
            this.addRenderableWidget(new Button.Builder(back, b -> this.onClose()).bounds(5, 5, 20, 20).tooltip(Tooltip.create(Component.translatable("message.charta.go_back"))).build());
        }

        float cardWidth = CardSlot.getWidth(CardSlot.Type.DEFAULT);
        float maxWidth = width - 60f - cardWidth;
        float maxLeftOffset = cardWidth + cardWidth/10f;

        float maxHeight = height - 80f;
        float cardHeight = CardSlot.getHeight(CardSlot.Type.DEFAULT);
        float maxTopOffset = cardHeight + cardHeight/10f;

        float topOffset = cardHeight + Math.max(0f, maxHeight - (deck.getUniqueSuits().size() * cardHeight)/(float) deck.getUniqueSuits().size());
        float totalHeight = cardHeight + (topOffset * (deck.getUniqueSuits().size() - 1f));
        float topExcess = totalHeight - maxHeight;
        if(topExcess > 0) {
            topOffset -= topExcess / (deck.getUniqueSuits().size() - 1f);
        }

        totalHeight = cardHeight + (maxTopOffset * (deck.getUniqueSuits().size() - 1f));
        float top = 0;
        if(topOffset > maxTopOffset) {
            top = Math.max(topOffset - maxTopOffset, (maxHeight - totalHeight));
            topOffset = maxTopOffset;
        }

        headerOffset = Math.max(0, Mth.floor(top/2 + 15));
        footerOffset = Math.max(0, Mth.floor(top/2 + 27));

        int i = 0;
        for(Suit suit : deck.getUniqueSuits()) {
            List<Card> cards = deck.getCards().stream().filter(c -> c.suit().equals(suit)).sorted().toList();

            float leftOffset = cardWidth + Math.max(0f, maxWidth - (cards.size() * cardWidth)/(float) cards.size());
            float totalWidth = cardWidth + (leftOffset * (cards.size() - 1f));
            float leftExcess = totalWidth - maxWidth;
            if(leftExcess > 0) {
                leftOffset -= leftExcess / (cards.size() - 1f);
            }

            totalWidth = cardWidth + (maxLeftOffset * (cards.size() - 1f));
            float left = 0;
            if(leftOffset > maxLeftOffset) {
                left = Math.max(leftOffset - maxLeftOffset, (maxWidth - totalWidth));
                leftOffset = maxLeftOffset;
            }

            this.addRenderableWidget(new CardWidget(this, Card.BLANK, deck, 25 + left/2, 45+(topOffset*i) + top/2, 1f) {
                @Override
                public @Nullable String getCardTranslatableKey() {
                    return deck.getDeckTranslatableKey();
                }
            });
            int j = 0;
            for(Card card : cards) {
                CardWidget cardWidget = new CardWidget(this, card, deck, cardWidth+35+(leftOffset*j) + left/2, 45+(topOffset*i) + top/2, 1f);
                this.addRenderableWidget(cardWidget);
                j++;
            }
            i++;
        }
    }

    @Override
    protected void renderFg(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(font, title, width/2, headerOffset, 0xFFFFFFFF);
        int i = 0;
        int totalWidth = deck.getUniqueSuits().size()*16 - 3;
        for(Suit suit : deck.getUniqueSuits()) {
            ChartaGuiGraphics.blitSuitAndGlow(guiGraphics, deck, suit, width/2f - totalWidth/2f + (i*16), headerOffset+10, 0, 0, 13, 13, 13, 13);
            i++;
        }
        guiGraphics.drawCenteredString(font, Component.literal(deck.getCards().size() + " ").append(Component.translatable("charta.cards")).append(" | "+deck.getUniqueSuits().size()+" ").append(Component.translatable("charta.suits")), width/2, height-footerOffset, 0xFFFFFFFF);
    }

    @Override
    public void scheduleTooltip(Component component) {
        this.setTooltipForNextRenderPass(component);
    }

    @Override
    public void onClose() {
        if(this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

}
