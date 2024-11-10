package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    public CardWidget(@Nullable HoverableRenderable parent, @Nullable ResourceLocation cardId, @Nullable String cardTranslatableKey, int x, int y, float scale) {
        super(parent, cardId, cardTranslatableKey, x, y, scale);
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@NotNull ResourceLocation cardId) {
        return ChartaClient.getCardTexture(cardId);
    }

    public static void renderCard(ResourceLocation cardId, GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        AbstractCardWidget dummyWidget = new AbstractCardWidget(null, cardId, null, x, y, 1f) {
            @Override
            public @NotNull ResourceLocation getCardTexture(@NotNull ResourceLocation cardId) {
                return cardId;
            }
        };
        dummyWidget.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }

}
