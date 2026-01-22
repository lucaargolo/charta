package dev.lucaargolo.charta.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.charta.NeoForgeChartaMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NeoForgeModShaderManager extends ModShaderManager {

    private final Map<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> shadersToRegister = new HashMap<>();
    private final List<Runnable> onShaderReload = new ArrayList<>();

    @Override
    public void init() {
        super.init();
        NeoForgeChartaMod.getModBus().register(this);
    }

    @Override
    public void registerShader(ResourceLocation location, VertexFormat vertexFormat, Consumer<ShaderInstance> consumer) {
        this.shadersToRegister.put(location, Pair.of(vertexFormat, consumer));
    }

    @Override
    public void registerEventOnShaderReload(Runnable runnable) {
        this.onShaderReload.add(runnable);
    }

    @SubscribeEvent
    public void registerShaders(RegisterShadersEvent event) throws IOException {
        for (Runnable runnable : this.onShaderReload) {
            runnable.run();
        }
        for (Map.Entry<ResourceLocation, Pair<VertexFormat, Consumer<ShaderInstance>>> entry : this.shadersToRegister.entrySet()) {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), entry.getKey(), entry.getValue().getLeft()), entry.getValue().getRight());
        }
    }

}
