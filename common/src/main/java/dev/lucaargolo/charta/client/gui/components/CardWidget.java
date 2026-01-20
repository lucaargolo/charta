package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.Deck;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    private final ResourceLocation glowTextureId;

    public CardWidget(@Nullable HoverableRenderable parent, Card card, Deck deck, float x, float y, float scale) {
        super(parent, card.flipped() ? deck.getDeckTexture(false) : deck.getCardTexture(card, false), deck.getCardTranslatableKey(card), deck.getCardColor(card), x, y, scale);
        this.glowTextureId = card.flipped() ? deck.getDeckTexture(true) : deck.getCardTexture(card, true);
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@Nullable ResourceLocation cardId, boolean glow) {
        if(glow && IrisCompat.isPresent()) {
            return glowTextureId != null ? glowTextureId : ChartaMod.MISSING_CARD;
        }else{
            return cardId != null ? cardId : ChartaMod.MISSING_CARD;
        }
    }

}
