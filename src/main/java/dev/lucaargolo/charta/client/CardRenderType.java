package dev.lucaargolo.charta.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CardRenderType extends RenderType {

    public CardRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final RenderStateShard.ShaderStateShard CARD_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaClient.CARD_SHADER);

    public static RenderType getCardType(ResourceLocation id) {
        return create("card",DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true,
            RenderType.CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(CARD_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(id, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(true)
        );
    }


}
