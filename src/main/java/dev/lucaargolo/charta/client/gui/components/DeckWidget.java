package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class DeckWidget extends AbstractCardWidget{

    public DeckWidget(@Nullable HoverableRenderable parent, ResourceLocation cardId, int x, int y, float scale) {
        super(parent, cardId, x, y, scale);
    }

    @Override
    public ResourceLocation getCardTexture(ResourceLocation cardId) {
        return ChartaClient.getDeckTexture(cardId);
    }

}
