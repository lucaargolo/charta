package dev.lucaargolo.charta.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerOptionData extends SavedData {

    private final HashMap<UUID, HashMap<ResourceLocation, byte[]>> playerOptions = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        for (Map.Entry<UUID, HashMap<ResourceLocation, byte[]>> playerEntry : playerOptions.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            UUID playerUUID = playerEntry.getKey();

            for (Map.Entry<ResourceLocation, byte[]> resourceEntry : playerEntry.getValue().entrySet()) {
                ResourceLocation resource = resourceEntry.getKey();
                byte[] data = resourceEntry.getValue();

                playerTag.putByteArray(resource.toString(), data);
            }

            tag.put(playerUUID.toString(), playerTag);
        }
        return tag;
    }

    public HashMap<ResourceLocation, byte[]> getPlayerOptions(ServerPlayer player) {
        return this.playerOptions.getOrDefault(player.getUUID(), new HashMap<>());
    }

    public void setPlayerOptions(ServerPlayer player, HashMap<ResourceLocation, byte[]> playerOptions) {
        this.playerOptions.put(player.getUUID(), playerOptions);
        this.setDirty();
    }

    public static PlayerOptionData load(@NotNull CompoundTag tag) {
        PlayerOptionData data = new PlayerOptionData();
        for (String playerUUIDString : tag.getAllKeys()) {
            UUID playerUUID = UUID.fromString(playerUUIDString);
            CompoundTag playerTag = tag.getCompound(playerUUIDString);

            HashMap<ResourceLocation, byte[]> resourceMap = new HashMap<>();

            for (String resourceKey : playerTag.getAllKeys()) {
                ResourceLocation resourceLocation = new ResourceLocation(resourceKey);
                byte[] array = playerTag.getByteArray(resourceKey);
                resourceMap.put(resourceLocation, array);
            }

            data.playerOptions.put(playerUUID, resourceMap);
        }
        return data;
    }
}
