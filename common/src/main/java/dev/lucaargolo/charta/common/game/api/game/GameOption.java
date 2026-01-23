package dev.lucaargolo.charta.common.game.api.game;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GameOption<T> {

    private final Component title;
    private final Component description;

    @Nullable Consumer<T> consumer;
    private byte value;

    public GameOption(T value, Component title, Component description) {
        this.value = toByte(value);
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
        if(consumer != null)
            consumer.accept(value);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
        if(consumer != null)
            consumer.accept(this.get());
    }

    public abstract Widget getWidget(Consumer<T> consumer, Font font, int width, int height, boolean showcase);

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

        @Nullable
        public Tooltip getTooltip() {
            return this.widget.getTooltip();
        }

        public void setTooltip(@Nullable Tooltip tooltip) {
            this.widget.setTooltip(tooltip);
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

        public Widget getWidget(Consumer<Boolean> consumer, Font font, int width, int height, boolean showcase) {
            Checkbox.Builder builder = Checkbox.builder(this.getTitle(), font);
            builder.tooltip(Tooltip.create(this.getDescription()));
            builder.maxWidth(width);
            builder.selected(this.get());
            builder.onValueChange((checkbox, value) -> this.set(value));
            Checkbox checkbox = builder.build();
            this.consumer = b -> {
                if(b != checkbox.selected()) checkbox.onPress();
                consumer.accept(this.get());
            };
            checkbox.active = !showcase;
            return new Widget(checkbox);
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

        public Widget getWidget(Consumer<Integer> consumer, Font font, int width, int height, boolean showcase) {
            Function<Integer, Component> message = (i) -> this.getTitle().copy().append(": ").append(Integer.toString(i));
            AbstractSliderButton slider = new AbstractSliderButton(0, 0, width, height, message.apply(this.get()), this.get() * (1.0/(max - min))) {
                private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");

                @Override
                @NotNull
                public ResourceLocation getHandleSprite() {
                    return showcase ? SLIDER_HANDLE_SPRITE : super.getHandleSprite();
                }

                @Override
                protected void updateMessage() {
                    this.setMessage(message.apply(Number.this.get()));
                }

                @Override
                protected void applyValue() {
                    Number.this.set(Mth.floor(Mth.lerp(this.value, min, max)));
                }

                @Override
                protected void renderScrollingString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int width, int color) {
                    super.renderScrollingString(guiGraphics, font, width, 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
                }
            };
            slider.setTooltip(Tooltip.create(this.getDescription()));
            this.consumer = i -> {
                slider.setValue(i * (1.0/(max - min)));
                consumer.accept(i);
            };
            slider.active = !showcase;
            return new Widget(slider);
        }

    }

}
