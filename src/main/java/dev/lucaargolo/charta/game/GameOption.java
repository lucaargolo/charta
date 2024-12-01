package dev.lucaargolo.charta.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public abstract class GameOption<T> {

    private final Component title;
    private final Component description;

    private byte value;

    public GameOption(T defaultValue, Component title, Component description) {
        this.value = toByte(defaultValue);
        this.title = title;
        this.description = description;
    }

    protected abstract byte toByte(T value);
    protected abstract T fromByte(byte value);

    public Component getTitle() {
        return title;
    }

    public Component getDescription() {
        return description;
    }

    public T get() {
        return fromByte(value);
    }

    public void set(T value) {
        this.value = toByte(value);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract Widget getWidget(Font font, int width, int height);

    @OnlyIn(Dist.CLIENT)
    public static class Widget extends ContainerObjectSelectionList.Entry<Widget> {

        private final AbstractWidget widget;

        public Widget(AbstractWidget widget) {
            this.widget = widget;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(widget);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(widget);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            widget.setX(left);
            widget.setY(top);
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

    }

    public static class Bool extends GameOption<Boolean> {

        public Bool(boolean value, Component title, Component description) {
            super(value, title, description);
        }

        @Override
        protected byte toByte(Boolean value) {
            return value ? (byte) 1: (byte) 0;
        }

        @Override
        protected Boolean fromByte(byte value) {
            return value == 1;
        }

        @OnlyIn(Dist.CLIENT)
        public Widget getWidget(Font font, int width, int height) {
            Checkbox.Builder builder = Checkbox.builder(this.getTitle(), font);
            builder.tooltip(Tooltip.create(this.getDescription()));
            builder.maxWidth(width);
            builder.selected(this.get());
            builder.onValueChange((checkbox, value) -> this.set(value));
            return new Widget(builder.build());
        }

    }

    public static class Number extends GameOption<Integer> {

        private final int min;
        private final int max;

        public Number(int value, int min, int max, Component title, Component description) {
            super(value, title, description);
            this.min = min;
            this.max = max;
        }

        @Override
        protected byte toByte(Integer value) {
            return value.byteValue();
        }

        @Override
        protected Integer fromByte(byte value) {
            return (int) value;
        }

        @OnlyIn(Dist.CLIENT)
        public Widget getWidget(Font font, int width, int height) {
            Function<Integer, Component> message = (i) -> this.getTitle().copy().append(": ").append(Integer.toString(i));
            AbstractSliderButton slider = new AbstractSliderButton(0, 0, width, height, message.apply(this.get()), this.get()) {

                @Override
                protected void updateMessage() {
                    this.setMessage(message.apply(Number.this.get()));
                }

                @Override
                protected void applyValue() {
                    Number.this.set(Mth.floor(Mth.lerp(this.value, min, max)));
                }

            };
            return new Widget(slider);
        }

    }

}
