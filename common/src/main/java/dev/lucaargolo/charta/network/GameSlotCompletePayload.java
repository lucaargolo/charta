package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.utils.ExpandedStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record GameSlotCompletePayload(BlockPos pos, int index, List<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack, boolean centered) implements CustomPacketPayload  {

    public GameSlotCompletePayload(BlockPos pos, int index, GameSlot slot) {
        this(pos, index, slot.stream().toList(), slot.getX(), slot.getY(), slot.getZ(), slot.getAngle(), slot.getStackDirection(), slot.getMaxStack(), slot.isCentered());
    }

    public static final CustomPacketPayload.Type<GameSlotCompletePayload> TYPE = new CustomPacketPayload.Type<>(Charta.id("game_slot_complete"));

    public static StreamCodec<ByteBuf, GameSlotCompletePayload> STREAM_CODEC = ExpandedStreamCodec.composite(
            BlockPos.STREAM_CODEC,
            GameSlotCompletePayload::pos,
            ByteBufCodecs.INT,
            GameSlotCompletePayload::index,
            ByteBufCodecs.collection(ArrayList::new, Card.STREAM_CODEC),
            GameSlotCompletePayload::cards,
            ByteBufCodecs.FLOAT,
            GameSlotCompletePayload::x,
            ByteBufCodecs.FLOAT,
            GameSlotCompletePayload::y,
            ByteBufCodecs.FLOAT,
            GameSlotCompletePayload::z,
            ByteBufCodecs.FLOAT,
            GameSlotCompletePayload::angle,
            Direction.STREAM_CODEC,
            GameSlotCompletePayload::stackDirection,
            ByteBufCodecs.FLOAT,
            GameSlotCompletePayload::maxStack,
            ByteBufCodecs.BOOL,
            GameSlotCompletePayload::centered,
            GameSlotCompletePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(GameSlotCompletePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> updateGameSlot(payload));
    }

    @OnlyIn(Dist.CLIENT)
    private static void updateGameSlot(GameSlotCompletePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if(level != null) {
            level.getBlockEntity(payload.pos, ModBlockEntityTypes.CARD_TABLE.get()).ifPresent(cardTable -> {
                List<Card> list = new LinkedList<>(payload.cards);
                if(payload.index == cardTable.getSlotCount()) {
                    cardTable.addSlot(new GameSlot(list, payload.x, payload.y, payload.z, payload.angle, payload.stackDirection, payload.maxStack, payload.centered));
                }else{
                    GameSlot tracked = cardTable.getSlot(payload.index);
                    tracked.setCards(list);
                    tracked.setX(payload.x);
                    tracked.setY(payload.y);
                    tracked.setZ(payload.z);
                    tracked.setAngle(payload.angle);
                    tracked.setStackDirection(payload.stackDirection);
                    tracked.setMaxStack(payload.maxStack);
                    tracked.setCentered(payload.centered);
                }
            });
        }
    }

}
