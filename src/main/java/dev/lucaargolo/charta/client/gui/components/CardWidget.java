package dev.lucaargolo.charta.client.gui.components;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.HoverableRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CardWidget extends AbstractCardWidget {

    public CardWidget(@Nullable HoverableRenderable parent, ResourceLocation cardId, int x, int y, float scale) {
        super(parent, cardId, x, y, scale);
    }

    @Override
    public ResourceLocation getCardTexture(ResourceLocation cardId) {
        return ChartaClient.getCardTexture(cardId);
    }

    public static void renderCard(ResourceLocation cardId, GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        AbstractCardWidget dummyWidget = new AbstractCardWidget(null, cardId, x, y, 1f) {
            @Override
            public ResourceLocation getCardTexture(ResourceLocation cardId) {
                return cardId;
            }
        };
        dummyWidget.renderWidget(graphics, mouseX, mouseY, partialTicks);
    }

}
