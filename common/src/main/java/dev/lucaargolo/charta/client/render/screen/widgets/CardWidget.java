package dev.lucaargolo.charta.client.render.screen.widgets;

import dev.lucaargolo.charta.client.compat.IrisCompat;
import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.game.api.card.Deck;
import dev.lucaargolo.charta.common.utils.HoverableRenderable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    private final ResourceLocation glowTextureId;

    public CardWidget(@Nullable HoverableRenderable parent, Card card, Deck deck, float x, float y, float scale) {
        super(parent, card.flipped() ? deck.getTexture(false) : deck.getCardTexture(card, false), deck.getCardTranslatableKey(card), deck.getCardColor(card), x, y, scale);
        this.glowTextureId = card.flipped() ? deck.getTexture(true) : deck.getCardTexture(card, true);
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
