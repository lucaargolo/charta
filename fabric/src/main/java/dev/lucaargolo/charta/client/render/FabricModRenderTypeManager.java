package dev.lucaargolo.charta.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class FabricModRenderTypeManager extends ModRenderTypeManager {

    protected RenderType createComposite(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeState state) {
        return create(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, state);
    }

}
