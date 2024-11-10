package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeckWidget extends AbstractCardWidget {

    public DeckWidget(@Nullable HoverableRenderable parent, @Nullable ResourceLocation cardId, @Nullable String cardTranslatableKey, int x, int y, float scale) {
        super(parent, cardId, cardTranslatableKey, x, y, scale);
    }

    @Override
    public @NotNull ResourceLocation getCardTexture(@NotNull ResourceLocation cardId) {
        return ChartaClient.getDeckTexture(cardId);
    }

}
