package dev.lucaargolo.charta.network;

import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.GameSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameSlotCompletePayload implements CustomPacketPayload  {

    private final BlockPos pos;
    private final int index;
    private final List<Card> cards;
    private final float x;
    private final float y;
    private final float z;
    private final float angle;
    private final Direction stackDirection;
    private final float maxStack;
    private final boolean centered;

    public GameSlotCompletePayload(BlockPos pos, int index, List<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack, boolean centered) {
        this.pos = pos;
        this.index = index;
        this.cards = cards;
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        this.stackDirection = stackDirection;
        this.maxStack = maxStack;
        this.centered = centered;
    }

    public GameSlotCompletePayload(BlockPos pos, int index, GameSlot slot) {
        this(pos, index, slot.stream().toList(), slot.getX(), slot.getY(), slot.getZ(), slot.getAngle(), slot.getStackDirection(), slot.getMaxStack(), slot.isCentered());
    }

    public GameSlotCompletePayload(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.index = buf.readInt();
        this.cards = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            Card card = Card.fromBuf(buf);
            cards.add(card);
        }
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
        this.angle = buf.readFloat();
        this.stackDirection = buf.readEnum(Direction.class);
        this.maxStack = buf.readFloat();
        this.centered = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeInt(cards.size());
        for (Card card : cards) {
            card.toBuf(buf);
        }
        buf.writeInt(index);
        buf.writeInt(cards.size());
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(angle);
        buf.writeEnum(stackDirection);
        buf.writeFloat(maxStack);
        buf.writeBoolean(centered);
    }


    public static void handleClient(GameSlotCompletePayload payload, NetworkEvent.Context context) {
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
