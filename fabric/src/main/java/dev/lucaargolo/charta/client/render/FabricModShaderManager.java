package dev.lucaargolo.charta.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FabricModShaderManager extends ModShaderManager {

    private final Map<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> shadersToRegister = new HashMap<>();
    private final List<Runnable> onShaderReload = new ArrayList<>();

    @Override
    public void init() {
        super.init();
        CoreShaderRegistrationCallback.EVENT.register(this::registerShaders);
    }

    @Override
    public void registerShader(ResourceLocation location, VertexFormat vertexFormat, Consumer<ShaderInstance> consumer) {
        this.shadersToRegister.put(location, Pair.of(vertexFormat, consumer));
    }

    @Override
    public void registerEventOnShaderReload(Runnable runnable) {
        this.onShaderReload.add(runnable);
    }

    private void registerShaders(CoreShaderRegistrationCallback.RegistrationContext context) throws IOException {
        for (Runnable runnable : this.onShaderReload) {
            runnable.run();
        }
        for (Map.Entry<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> entry : this.shadersToRegister.entrySet()) {
            context.register(entry.getKey(), entry.getValue().getLeft(), entry.getValue().getRight());
        }
    }

}
