package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.resources.ResourceLocation;

public class DeckWidget extends AbstractCardWidget{

    public DeckWidget(HoverableRenderable parent, ResourceLocation cardId, int x, int y, float scale) {
        super(parent, cardId, x, y, scale);
    }

    @Override
    public ResourceLocation getCardTexture(ResourceLocation cardId) {
        return ChartaClient.getDeckTexture(cardId);
    }

}
