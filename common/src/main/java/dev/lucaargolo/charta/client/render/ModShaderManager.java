package dev.lucaargolo.charta.client.render;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lucaargolo.charta.ChartaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ModShaderManager {

    private static final ResourceLocation BLUR_LOCATION = ChartaMod.id("shaders/post/blur.json");

    private final List<Consumer<Float>> cardFovUniforms = new ArrayList<>();
    private final Consumer<Float> cardFov = f -> cardFovUniforms.forEach(c -> c.accept(f));

    private final List<Consumer<Float>> cardXRotUniforms = new ArrayList<>();
    private final Consumer<Float> cardXRot = f -> cardXRotUniforms.forEach(c -> c.accept(f));

    private final List<Consumer<Float>> cardYRotUniforms = new ArrayList<>();
    private final Consumer<Float> cardYRot = f -> cardYRotUniforms.forEach(c -> c.accept(f));

    private final List<Consumer<Float>> cardInsetUniforms = new ArrayList<>();
    private final Consumer<Float> cardInset = f -> cardInsetUniforms.forEach(c -> c.accept(f));

    private RenderTarget glowRenderTarget;
    private PostChain glowBlurEffect;

    private ShaderInstance imageShader;
    private ShaderInstance imageGlowShader;
    private ShaderInstance imageArgbShader;
    private ShaderInstance whiteImageShader;
    private ShaderInstance whiteImageGlowShader;
    private ShaderInstance whiteImageArgbShader;
    private ShaderInstance cardShader;
    private ShaderInstance cardGlowShader;
    private ShaderInstance cardArgbShader;
    private ShaderInstance perspectiveShader;
    private ShaderInstance grayscaleShader;
    private ShaderInstance entityCardShader;
    private ShaderInstance ironLeashShader;

    public void init() {
        this.registerShader(ChartaMod.id("image"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.imageShader = instance;
        });
        this.registerShader(ChartaMod.id("image_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.imageGlowShader = instance;
        });
        this.registerShader(ChartaMod.id("image_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.imageArgbShader = instance;
        });
        this.registerShader(ChartaMod.id("white_image"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.whiteImageShader = instance;
        });
        this.registerShader(ChartaMod.id("white_image_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.whiteImageGlowShader = instance;
        });
        this.registerShader(ChartaMod.id("white_image_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.whiteImageArgbShader = instance;
        });
        this.registerShader(ChartaMod.id("card"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
            this.cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
            this.cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
            this.cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
            this.cardShader = instance;
        });
        this.registerShader(ChartaMod.id("card_glow"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
            this.cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
            this.cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
            this.cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
            this.cardGlowShader = instance;
        });
        this.registerShader(ChartaMod.id("card_argb"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
            this.cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
            this.cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
            this.cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
            this.cardArgbShader = instance;
        });
        this.registerShader(ChartaMod.id("perspective"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.cardFovUniforms.add(Objects.requireNonNull(instance.getUniform("Fov"))::set);
            this.cardXRotUniforms.add(Objects.requireNonNull(instance.getUniform("XRot"))::set);
            this.cardYRotUniforms.add(Objects.requireNonNull(instance.getUniform("YRot"))::set);
            this.cardInsetUniforms.add(Objects.requireNonNull(instance.getUniform("InSet"))::set);
            this.perspectiveShader = instance;
        });
        this.registerShader(ChartaMod.id("grayscale"), DefaultVertexFormat.POSITION_TEX_COLOR, instance -> {
            this.grayscaleShader = instance;
        });
        this.registerShader(ChartaMod.id("rendertype_entity_card"), DefaultVertexFormat.NEW_ENTITY, instance -> {
            this.entityCardShader = instance;
        });
        this.registerShader(ChartaMod.id("rendertype_iron_leash"), DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, instance -> {
            this.ironLeashShader = instance;
        });
        this.registerEventOnShaderReload(this::onShaderReload);
    }

    protected abstract void registerShader(ResourceLocation location, VertexFormat vertexFormat, Consumer<ShaderInstance> consumer);

    public abstract void registerEventOnShaderReload(Runnable runnable);

    public final void onShaderReload() {
        Minecraft minecraft = Minecraft.getInstance();

        this.cardFovUniforms.clear();
        this.cardXRotUniforms.clear();
        this.cardYRotUniforms.clear();
        this.cardInsetUniforms.clear();

        if (this.glowRenderTarget == null) {
            this.glowRenderTarget = new TextureTarget(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), false, Minecraft.ON_OSX);
            this.glowRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.glowRenderTarget.clear(Minecraft.ON_OSX);
        }

        if (this.glowBlurEffect != null) {
            this.glowBlurEffect.close();
        }

        try {
            assert this.glowRenderTarget != null;
            this.glowBlurEffect = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), this.glowRenderTarget, BLUR_LOCATION);
            this.glowBlurEffect.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        } catch (IOException ioException) {
            ChartaMod.LOGGER.warn("Failed to load shader: {}", BLUR_LOCATION, ioException);
        } catch (JsonSyntaxException jsonSyntaxException) {
            ChartaMod.LOGGER.warn("Failed to parse shader: {}", BLUR_LOCATION, jsonSyntaxException);
        }

    }

    public void processBlurEffect(float partialTick) {
        float f = 2f;
        if (this.glowBlurEffect != null) {
            this.glowBlurEffect.setUniform("Radius", f);
            this.glowBlurEffect.process(partialTick);
        }
    }

    public Consumer<Float> getCardInset() {
        return cardInset;
    }

    public Consumer<Float> getCardYRot() {
        return cardYRot;
    }

    public Consumer<Float> getCardXRot() {
        return cardXRot;
    }

    public Consumer<Float> getCardFov() {
        return cardFov;
    }

    public RenderTarget getGlowRenderTarget() {
        return this.glowRenderTarget;
    }

    public PostChain getGlowBlurEffect() {
        return this.glowBlurEffect;
    }

    public ShaderInstance getImageShader() {
        return this.imageShader;
    }

    public ShaderInstance getImageGlowShader() {
        return this.imageGlowShader;
    }

    public ShaderInstance getImageArgbShader() {
        return this.imageArgbShader;
    }

    public ShaderInstance getWhiteImageShader() {
        return this.whiteImageShader;
    }

    public ShaderInstance getWhiteImageGlowShader() {
        return this.whiteImageGlowShader;
    }

    public ShaderInstance getWhiteImageArgbShader() {
        return this.whiteImageArgbShader;
    }

    public ShaderInstance getCardShader() {
        return this.cardShader;
    }

    public ShaderInstance getCardGlowShader() {
        return this.cardGlowShader;
    }

    public ShaderInstance getCardArgbShader() {
        return this.cardArgbShader;
    }

    public ShaderInstance getPerspectiveShader() {
        return this.perspectiveShader;
    }

    public ShaderInstance getGrayscaleShader() {
        return this.grayscaleShader;
    }

    public ShaderInstance getEntityCardShader() {
        return entityCardShader;
    }

    public ShaderInstance getIronLeashShader() {
        return ironLeashShader;
    }

}
