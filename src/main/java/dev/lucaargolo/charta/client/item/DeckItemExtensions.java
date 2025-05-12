package dev.lucaargolo.charta.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ModRenderType;
import dev.lucaargolo.charta.compat.IrisCompat;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.item.CardDeckItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DeckItemExtensions implements IClientItemExtensions {

    private static final RandomSource RANDOM = RandomSource.create();

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        Minecraft minecraft = Minecraft.getInstance();
        return new BlockEntityWithoutLevelRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels()) {
            @Override
            @SuppressWarnings("deprecation")
            public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
                BakedModel model = minecraft.getModelManager().getModel(Charta.id("deck"));
                List<BakedQuad> transformedQuads = model.getQuads(null, null, RANDOM).stream().map(DeckItemExtensions::replaceQuadSprite).toList();

                CardDeck deck = CardDeckItem.getDeck(stack);

                if(IrisCompat.isPresent()) {
                    ResourceLocation deckGlowTexture = deck != null ? deck.getDeckTexture(true) : Charta.MISSING_CARD;
                    RenderType glowRenderType = RenderType.entityTranslucentEmissive(deckGlowTexture);
                    minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(glowRenderType), transformedQuads, stack, LightTexture.FULL_BRIGHT, packedOverlay);
                }

                ResourceLocation deckTexture = deck != null ? deck.getDeckTexture(false) : Charta.MISSING_CARD;
                RenderType renderType = IrisCompat.isPresent() ? RenderType.entityTranslucent(deckTexture) : ModRenderType.entityCard(deckTexture);
                minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(renderType), transformedQuads, stack, packedLight, packedOverlay);

                if(IrisCompat.isPresent()) {
                    ResourceLocation deckGlowTexture = deck != null ? deck.getDeckTexture(true) : Charta.MISSING_CARD;
                    RenderType glowRenderType = RenderType.entityTranslucentEmissive(deckGlowTexture);
                    minecraft.getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(glowRenderType), transformedQuads, stack, LightTexture.FULL_BRIGHT, packedOverlay);
                }
            }
        };
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
