package dev.lucaargolo.charta.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.client.ChartaModClient;
import dev.lucaargolo.charta.client.compat.IrisCompat;
import dev.lucaargolo.charta.game.Deck;
import dev.lucaargolo.charta.item.DeckItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeckItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final RandomSource RANDOM = RandomSource.create();

    private final Minecraft minecraft;

    public DeckItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel model = this.minecraft.getModelManager().getModel(new ModelResourceLocation(ChartaMod.id("deck"), "standalone"));
        List<BakedQuad> transformedQuads = model.getQuads(null, null, RANDOM).stream().map(DeckItemRenderer::replaceQuadSprite).toList();

        Deck deck = DeckItem.getDeck(stack);

        if(IrisCompat.isPresent()) {
            ResourceLocation deckGlowTexture = deck != null ? deck.getDeckTexture(true) : ChartaMod.MISSING_CARD;
            RenderType glowRenderType = RenderType.entityTranslucentEmissive(deckGlowTexture);
            this.minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(glowRenderType), transformedQuads, stack, LightTexture.FULL_BRIGHT, packedOverlay);
        }

        ResourceLocation deckTexture = deck != null ? deck.getDeckTexture(false) : ChartaMod.MISSING_CARD;
        RenderType renderType = IrisCompat.isPresent() ? RenderType.entityTranslucent(deckTexture) : ChartaModClient.getRenderTypeManager().entityCard(deckTexture);
        this.minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(renderType), transformedQuads, stack, packedLight, packedOverlay);

        if(IrisCompat.isPresent()) {
            ResourceLocation deckGlowTexture = deck != null ? deck.getDeckTexture(true) : ChartaMod.MISSING_CARD;
            RenderType glowRenderType = RenderType.entityTranslucentEmissive(deckGlowTexture);
            this.minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(glowRenderType), transformedQuads, stack, LightTexture.FULL_BRIGHT, packedOverlay);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static BakedQuad replaceQuadSprite(BakedQuad quad) {
        int[] vertexData = quad.getVertices().clone();
        for(int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
            int i = cornerIndex * 8;
            float uFrame = ((Float.intBitsToFloat(vertexData[i + 4]) - quad.getSprite().getU0())/(quad.getSprite().getU1() - quad.getSprite().getU0()));
            vertexData[i + 4] = Float.floatToRawIntBits(uFrame);
            float vFrame = ((Float.intBitsToFloat(vertexData[i + 5]) - quad.getSprite().getV0())/(quad.getSprite().getV1() - quad.getSprite().getV0()));
            vertexData[i + 5] = Float.floatToRawIntBits(vFrame);
        }
        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), null, quad.isShade());
    }

}
