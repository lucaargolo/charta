package dev.lucaargolo.charta.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderType extends RenderType {

    public ModRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final RenderStateShard.ShaderStateShard ENTITY_CARD_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaClient.ENTITY_CARD_SHADER);
    private static final RenderStateShard.ShaderStateShard CARD_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaClient.CARD_SHADER);
    private static final RenderStateShard.ShaderStateShard IRON_LEASH_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaClient.IRON_LEASH_SHADER);

    private static final RenderType IRON_LEASH = create(
            "iron_leash",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.TRIANGLE_STRIP,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(IRON_LEASH_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );

    public static RenderType entityCard(ResourceLocation id) {
        return create("entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
            RenderType.CompositeState.builder()
                .setShaderState(ENTITY_CARD_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(id, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true)
        );
    }

    public static RenderType card(ResourceLocation id) {
        return create("card", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true,
            RenderType.CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(CARD_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(id, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(true)
        );
    }

    public static RenderType ironLeash() {
        return IRON_LEASH;
    }


}
