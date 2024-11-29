package dev.lucaargolo.charta.utils;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.Card;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class GameSlot {

    private final Consumer<TransparentLinkedList<Card>> consumer = this::setCards;

    private CardTableBlockEntity parent = null;
    private int index = -1;

    private TransparentLinkedList<Card> cards;

    private float lastX;
    private float x;
    private float lastY;
    private float y;
    private float lastZ;
    private float z;
    private float lastAngle;
    private float angle;
    private Direction stackDirection;
    private float maxStack;

    public GameSlot(TransparentLinkedList<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack) {
        this.cards = cards;
        this.x = x;
        this.lastX = x;
        this.y = y;
        this.lastY = y;
        this.z = z;
        this.lastZ = z;
        this.angle = angle;
        this.lastAngle = angle;
        this.stackDirection = stackDirection;
        this.maxStack = maxStack;
        this.cards.addConsumer(consumer);
    }

    public GameSlot(TransparentLinkedList<Card> cards, float x, float y, float z, float angle, Direction stackDirection) {
        this(cards, x, y, z, angle, stackDirection, CardTableBlockEntity.TABLE_WIDTH/2f);
    }

    public GameSlot(TransparentLinkedList<Card> cards, float x, float y, float z, float angle) {
        this(cards, x, y, z, angle, Direction.UP);
    }

    public GameSlot(TransparentLinkedList<Card> cards) {
        this(cards, 0f, 0f, 0f, 0f);
    }

    public GameSlot() {
        this(new TransparentLinkedList<>());
    }

    public void setup(CardTableBlockEntity parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    public TransparentLinkedList<Card> getCards() {
        return cards;
    }


    public void setCards(TransparentLinkedList<Card> cards) {
        this.cards = cards;
        if(parent != null)
            parent.setGameSlotDirty(index, true);
    }

    public float getX() {
        return x;
    }

    public float lerpX(float partialTicks) {
        float newX = Mth.lerp(partialTicks, lastX, x);
        lastX = newX;
        return newX;
    }

    public void setX(float x) {
        this.x = x;
        if(parent != null)
            parent.setGameSlotDirty(index, false);
    }

    public float getY() {
        return y;
    }

    public float lerpY(float partialTicks) {
        float newY = Mth.lerp(partialTicks, lastY, y);
        lastY = newY;
        return newY;
    }

    public void setY(float y) {
        this.y = y;
        if(parent != null)
            parent.setGameSlotDirty(index, false);
    }

    public float getZ() {
        return z;
    }

    public float lerpZ(float partialTicks) {
        float newZ = Mth.lerp(partialTicks, lastZ, z);
        lastZ = newZ;
        return newZ;
    }

    public void setZ(float z) {
        this.z = z;
        if(parent != null)
            parent.setGameSlotDirty(index, false);
    }

    public float getAngle() {
        return angle;
    }

    public float lerpAngle(float partialTicks) {
        float newAngle = Mth.lerp(partialTicks, lastAngle, angle);
        lastAngle = newAngle;
        return newAngle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
        if(parent != null)
            parent.setGameSlotDirty(index, false);
    }

    public Direction getStackDirection() {
        return stackDirection;
    }

    public void setStackDirection(Direction stackDirection) {
        this.stackDirection = stackDirection;
    }

    public float getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(float maxStack) {
        this.maxStack = maxStack;
    }

    public void clear() {
        this.cards.removeConsumer(consumer);
    }
}
