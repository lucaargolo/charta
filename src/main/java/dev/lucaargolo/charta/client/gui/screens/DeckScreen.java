package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.client.gui.components.CardWidget;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeckScreen extends CardScreen implements HoverableRenderable {

    private final CardDeck deck;
    private final Set<Suit> suits;

    private int headerOffset = 10;
    private int footerOffset = 10;

    public DeckScreen(CardDeck deck) {
        super(deck.getName());
        this.deck = deck;
        this.suits = this.deck.getCards().stream().map(Card::getSuit).collect(Collectors.toSet());
    }

    @Override
    protected void init() {
        float cardWidth = CardSlot.getWidth(CardSlot.Type.DEFAULT);
        float maxWidth = width - 60f - cardWidth;
        float maxLeftOffset = cardWidth + cardWidth/10f;

        float maxHeight = height - 80f;
        float cardHeight = CardSlot.getHeight(CardSlot.Type.DEFAULT);
        float maxTopOffset = cardHeight + cardHeight/10f;

        float topOffset = cardHeight + Math.max(0f, maxHeight - (suits.size() * cardHeight)/(float) suits.size());
        float totalHeight = cardHeight + (topOffset * (suits.size() - 1f));
        float topExcess = totalHeight - maxHeight;
        if(topExcess > 0) {
            topOffset -= topExcess / (suits.size() - 1f);
        }

        totalHeight = cardHeight + (maxTopOffset * (suits.size() - 1f));
        float top = 0;
        if(topOffset > maxTopOffset) {
            top = Math.max(topOffset - maxTopOffset, (maxHeight - totalHeight));
            topOffset = maxTopOffset;
        }

        headerOffset = Math.max(0, Mth.floor(top/2 + 15));
        footerOffset = Math.max(0, Mth.floor(top/2 + 27));

        int i = 0;
        for(Suit suit : suits) {
            List<Card> cards = deck.getCards().stream().filter(c -> c.getSuit().equals(suit)).sorted().toList();

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
        int totalWidth = suits.size()*16 - 3;
        for(Suit suit : suits) {
            ChartaGuiGraphics.blitImageAndGlow(guiGraphics, deck.getSuitTexture(suit), width/2f - totalWidth/2f + (i*16), headerOffset+10, 0, 0, 13, 13, 13, 13);
            i++;
        }
        guiGraphics.drawCenteredString(font, Component.literal(deck.getCards().size() + " ").append(Component.translatable("charta.cards")).append(" | "+suits.size()+" ").append(Component.translatable("charta.suits")), width/2, height-footerOffset, 0xFFFFFFFF);
    }

    @Override
    public void scheduleTooltip(Component component) {
        this.setTooltipForNextRenderPass(component);
    }

}
