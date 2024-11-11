package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    public CardWidget(@Nullable HoverableRenderable parent, @Nullable ResourceLocation cardId, @Nullable String cardTranslatableKey, float x, float y, float scale) {
        super(parent, cardId, cardTranslatableKey, x, y, scale);
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@Nullable ResourceLocation cardId) {
        return cardId != null ? cardId : Charta.MISSING_CARD;
    }

    public static void renderCard(ResourceLocation cardId, GuiGraphics graphics, float x, float y, int mouseX, int mouseY, float partialTicks) {
        AbstractCardWidget dummyWidget = new CardWidget(null, cardId, null, x, y, 1f);
        dummyWidget.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }

}
