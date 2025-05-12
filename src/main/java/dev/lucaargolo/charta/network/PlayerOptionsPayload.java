package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.utils.PlayerOptionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;

public class PlayerOptionsPayload implements CustomPacketPayload {

    private final HashMap<ResourceLocation, byte[]> playerOptions;

    public PlayerOptionsPayload(HashMap<ResourceLocation, byte[]> playerOptions) {
        this.playerOptions = playerOptions;
    }

    public PlayerOptionsPayload(FriendlyByteBuf buf) {
        this.playerOptions = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.playerOptions.put(buf.readResourceLocation(), buf.readByteArray());
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerOptions.size());
        this.playerOptions.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeByteArray(value);
        });
    }

    public static void handleBoth(PlayerOptionsPayload payload, NetworkEvent.Context context) {
        if(context.getDirection().getReceptionSide().isServer()) {
            handleServer(payload, context);
        }else{
            handleClient(payload, context);
        }
    }

    public static void handleClient(PlayerOptionsPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ChartaClient.LOCAL_OPTIONS.clear();
            ChartaClient.LOCAL_OPTIONS.putAll(payload.playerOptions);
        });
    }

    public static void handleServer(PlayerOptionsPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            PlayerOptionData data = context.getSender().server.overworld().getDataStorage().computeIfAbsent(PlayerOptionData::load, PlayerOptionData::new, "charta_player_options");
            data.setPlayerOptions(context.getSender(), payload.playerOptions);
        });
    }



}
