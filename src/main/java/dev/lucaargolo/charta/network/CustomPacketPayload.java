package dev.lucaargolo.charta.network;

import net.minecraft.network.FriendlyByteBuf;

public interface CustomPacketPayload {

    void toBytes(FriendlyByteBuf buf);

}
