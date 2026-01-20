package dev.lucaargolo.charta.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.charta.client.ChartaModClient;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class ModRenderTypeManager extends RenderType {

    private static final RenderStateShard.ShaderStateShard ENTITY_CARD_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaModClient.getShaderManager().getEntityCardShader());
    private static final RenderStateShard.ShaderStateShard IRON_LEASH_SHADER = new RenderStateShard.ShaderStateShard(() -> ChartaModClient.getShaderManager().getIronLeashShader());

    protected ModRenderTypeManager() {
        super("", DefaultVertexFormat.BLIT_SCREEN, VertexFormat.Mode.LINES, 0, false, false, () -> {}, () -> {});
    }

    protected abstract RenderType createComposite(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeState state);

    private final Function<ResourceLocation, RenderType> ENTITY_CARD = Util.memoize(location ->
            createComposite("entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(ENTITY_CARD_SHADER)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .createCompositeState(true)
            )
    );

    private final RenderType IRON_LEASH = createComposite(
            "iron_leash",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.TRIANGLE_STRIP,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(IRON_LEASH_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );


    public final RenderType entityCard(ResourceLocation id) {
        return ENTITY_CARD.apply(id);
    }

    public final RenderType ironLeash() {
        return IRON_LEASH;
    }

}
