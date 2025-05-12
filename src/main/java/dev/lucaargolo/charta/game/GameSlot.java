package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.sound.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GameSlot {

    private CardTableBlockEntity parent = null;
    private int index = -1;

    private List<Card> cards;

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
    private boolean centered;

    public int highlightColor = 0xFFFFFF;
    public int highlightTime = 0;

    public GameSlot(List<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack, boolean centered) {
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
        this.centered = centered;
    }

    public GameSlot(List<Card> cards, float x, float y, float z, float angle, Direction stackDirection, float maxStack) {
        this(cards, x, y, z, angle, stackDirection, maxStack, true);
    }

    public GameSlot(List<Card> cards, float x, float y, float z, float angle, Direction stackDirection) {
        this(cards, x, y, z, angle, stackDirection, CardTableBlockEntity.TABLE_WIDTH/2f);
    }

    public GameSlot(List<Card> cards, float x, float y, float z, float angle) {
        this(cards, x, y, z, angle, Direction.UP);
    }

    public GameSlot(List<Card> cards) {
        this(cards, 0f, 0f, 0f, 0f);
    }

    public GameSlot() {
        this(new LinkedList<>());
    }

    public boolean removeAll() {
        return true;
    }

    public void onInsert(CardPlayer player, List<Card> cards) {
        player.playSound(ModSounds.CARD_PLAY.get());
    }

    public void onRemove(CardPlayer player, List<Card> cards) {
        player.playSound(ModSounds.CARD_DRAW.get());
    }

    public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
        return true;
    }

    public boolean canRemoveCard(CardPlayer player, int index) {
        return !isEmpty();
    }

    public void setParent(CardTableBlockEntity parent) {
        this.parent = parent;
    }

    public void setDirty(boolean cards) {
        if(parent != null)
            parent.setSlotDirty(index, cards);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Iterable<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
        this.setDirty(true);
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
        this.setDirty(false);
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
        this.setDirty(false);
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
        this.setDirty(false);
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
        this.setDirty(false);
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

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public boolean isEmpty() {
        return this.cards.isEmpty();
    }

    public int size() {
        return this.cards.size();
    }

    public Card get(int i) {
        return this.cards.get(i);
    }

    public Card getLast() {
        return this.cards.get(cards.size()-1);
    }

    public void add(Card card) {
        this.cards.add(card);
        this.setDirty(true);
    }

    public void addLast(Card card) {
        this.cards.add(card);
        this.setDirty(true);
    }

    public boolean addAll(GameSlot slot) {
        return this.addAll(slot.cards);
    }

    public boolean addAll(Collection<Card> collection) {
        boolean result = this.cards.addAll(collection);
        this.setDirty(true);
        return result;
    }

    public boolean addAll(int i, GameSlot slot) {
        boolean result = this.cards.addAll(i, slot.cards);
        this.setDirty(true);
        return result;
    }

    public Card remove(int i) {
        Card result = this.cards.remove(i);
        this.setDirty(true);
        return result;
    }

    public boolean remove(Card card) {
        boolean result = this.cards.remove(card);
        this.setDirty(true);
        return result;
    }

    public Card removeLast() {
        Card last = this.cards.remove(this.cards.size()-1);
        this.setDirty(true);
        return last;
    }

    public boolean contains(Card card) {
        return this.cards.contains(card);
    }

    public void clear() {
        this.cards.clear();
        this.setDirty(true);
    }

    public void shuffle() {
        Collections.shuffle(this.cards);
        this.setDirty(true);
    }

    public void reverse() {
        Collections.reverse(this.cards);
        this.setDirty(true);
    }

    public void forEach(Consumer<Card> consumer) {
        this.cards.forEach(consumer);
    }

    public Stream<Card> stream() {
        return this.cards.stream();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GameSlot)) {
            return false;
        }
        return this.cards.equals(((GameSlot)obj).cards);
    }

    public static GameSlot copyOf(GameSlot slot) {
        LinkedList<Card> cards = new LinkedList<>();
        for(Card c : slot.cards) {
            cards.add(c.copy());
        }
        return new GameSlot(cards, slot.x, slot.y, slot.z, slot.angle, slot.stackDirection, slot.maxStack);
    }

}
