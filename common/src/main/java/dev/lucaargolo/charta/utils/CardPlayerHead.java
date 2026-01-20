package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.CardPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
        if(entity instanceof Player player) {
            return playerCache.computeIfAbsent(entity.getUUID(), u -> getPlayer(player));
        }else{
            return cache.computeIfAbsent(entity.getType(), t -> getOther(entity));
        }
    }

    private static CardPlayerHead getPlayer(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if(connection != null) {
            PlayerInfo info = connection.getPlayerInfo(player.getUUID());
            if(info != null) {
                return new CardPlayerHead(info.getSkin().texture(), 8f, 8f, 8f, 8f, 64f, 64f);
            }
        }
        return UNKNOWN;
    }

    @SuppressWarnings("deprecation")
    private static CardPlayerHead getOther(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation id = entity.getType().builtInRegistryHolder().key().location();
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/head/" + id.getPath() + ".png");
        AbstractTexture texture = minecraft.getTextureManager().getTexture(location);
        try {
            texture.load(minecraft.getResourceManager());
            return new CardPlayerHead(location, 0f, 0f, 8f, 8f, 8f, 8f);
        } catch (Exception ignored) {
            return UNKNOWN;
        }
    }

}
