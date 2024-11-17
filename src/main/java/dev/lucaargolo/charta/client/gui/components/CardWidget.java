package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    private final ResourceLocation glowTextureId;

    public CardWidget(@Nullable HoverableRenderable parent, Card card, CardDeck deck, float x, float y, float scale) {
        super(parent, card.isFlipped() ? deck.getDeckTexture(false) : deck.getCardTexture(card, false), deck.getCardTranslatableKey(card), x, y, scale);
        this.glowTextureId = card.isFlipped() ? deck.getDeckTexture(true) : deck.getCardTexture(card, true);
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@Nullable ResourceLocation cardId, boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return glowTextureId != null ? glowTextureId : Charta.MISSING_CARD;
        }else{
            return cardId != null ? cardId : Charta.MISSING_CARD;
        }
    }

    public static void renderCard(Card card, CardDeck deck, GuiGraphics graphics, float x, float y, int mouseX, int mouseY, float partialTicks) {
        AbstractCardWidget dummyWidget = new CardWidget(null, card, deck, x, y, 1f);
        dummyWidget.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }

}
