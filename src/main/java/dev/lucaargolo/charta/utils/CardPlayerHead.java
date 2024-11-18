package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CardPlayerHead(ResourceLocation texture, float u, float v, float uOffset, float vOffset, float width, float height) {

    public static final CardPlayerHead UNKNOWN = new CardPlayerHead(Charta.id("textures/misc/unknown.png"), 0f, 0f, 12f, 12f, 12f, 12f);
    public static final CardPlayerHead ROBOT = new CardPlayerHead(Charta.id("textures/misc/robot.png"), 0f, 0f, 12f, 12f, 12f, 12f);

    private static final Map<UUID, CardPlayerHead> playerCache = new HashMap<>();
    private static final Map<EntityType<?>, CardPlayerHead> cache = new HashMap<>();

    public static void renderHead(GuiGraphics graphics, int x, int y, CardPlayer player) {
        CardPlayerHead head = player.getHead();
        float xOffset = (24f - head.uOffset*2f)/2f;
        float yOffset = (24f - head.vOffset*2f)/2f;
        graphics.pose().pushPose();
        graphics.pose().translate((int) (x + xOffset), (int) (y + yOffset), 0f);
        graphics.pose().scale(2f, 2f, 2f);
        graphics.blit(head.texture(), 0, 0, head.u(), head.v(), (int) head.uOffset(), (int) head.vOffset(), (int) head.width(), (int) head.height());
        graphics.pose().popPose();
    }

    public static CardPlayerHead get(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if(type.equals(EntityType.PLAYER)) {
            return playerCache.computeIfAbsent(entity.getUUID(), u -> innerGet(entity));
        }else{
            return cache.computeIfAbsent(type, t -> innerGet(entity));
        }
    }

    /*
        This is a massive hack to get the head texture of an entity.
        It works by assuming lots of things:
            - The existence of a ModelPart called head in the entity model.
            - That the head ModelPart contains a single cube
            - That the single cube contains the head texture at the polygons[3]
        It works... Kinda....
     */

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static CardPlayerHead innerGet(LivingEntity entity) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            LivingEntityRenderer renderer = (LivingEntityRenderer) minecraft.getEntityRenderDispatcher().getRenderer(entity);
            EntityModel<?> model = renderer.getModel();
            Class<?> modelClass = model.getClass();
            Field headField = null;
            boolean last = false;
            boolean bone = false;
            while (headField == null && modelClass.getSuperclass() != Object.class) {
                for (Field f : modelClass.getFields()) {
                    if (f.getName().contains("head") && f.getType() == ModelPart.class) {
                        headField = f;
                        break;
                    }
                }
                for (Field f : modelClass.getDeclaredFields()) {
                    if (f.getName().contains("head")  && f.getType() == ModelPart.class) {
                        headField = f;
                        break;
                    }
                }
                modelClass = modelClass.getSuperclass();
            }
            if (headField == null) {
                try {
                    headField = model.getClass().getDeclaredField("bone");
                    bone = true;
                } catch (NoSuchFieldException e) {
                    headField = model.getClass().getDeclaredField("root");
                    last = true;
                }
            }
            headField.setAccessible(true);
            ModelPart part = (ModelPart) headField.get(model);
            if (bone) {
                part = part.getChild("body");
            }
            ModelPart.Cube cube;
            if (!part.cubes.isEmpty()) {
                cube = part.cubes.getFirst();
            } else {
                while (part.cubes.isEmpty() && !part.children.isEmpty()) {
                    if (last)
                        part = part.children.values().stream().reduce((first, second) -> second).get();
                    else
                        part = part.children.values().stream().findFirst().get();
                }
                cube = part.cubes.getFirst();
            }
            ModelPart.Polygon polygon = cube.polygons[3];
            float minU = Float.MAX_VALUE;
            float maxU = Float.MIN_VALUE;
            float minV = Float.MAX_VALUE;
            float maxV = Float.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                ModelPart.Vertex vertex = polygon.vertices[i];
                if (vertex.u > maxU) {
                    maxU = vertex.u;
                }
                if (vertex.v > maxV) {
                    maxV = vertex.v;
                }
                if (vertex.u < minU) {
                    minU = vertex.u;
                }
                if (vertex.v < minV) {
                    minV = vertex.v;
                }
            }
            int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            ResourceLocation location = renderer.getTextureLocation(entity);
            AbstractTexture texture = minecraft.getTextureManager().getTexture(location);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
            int textureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int textureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);
            return new CardPlayerHead(location, minU * textureWidth, minV * textureHeight, (maxU - minU) * textureWidth, (maxV - minV) * textureHeight, textureWidth, textureHeight);
        } catch (Exception e) {
            e.printStackTrace();
            return UNKNOWN;
        }
    }

}
