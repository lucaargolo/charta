package dev.lucaargolo.charta.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomOptionTooltip extends Tooltip {

    @Nullable
    private final Tooltip fallback;

    private final String original;
    private final String current;

    public CustomOptionTooltip(@Nullable Tooltip fallback, String original, String current) {
        super(fallback != null ? fallback.message : Component.empty(), fallback != null ? fallback.narration : Component.empty());
        if(fallback instanceof CustomOptionTooltip customOptionTooltip) {
            this.fallback = customOptionTooltip.fallback;
        }else {
            this.fallback = fallback;
        }
        this.original = original;
        this.current = current;
    }

    @Override
    public @NotNull List<FormattedCharSequence> toCharSequence(@NotNull Minecraft minecraft) {
        if(original.equals(current) && fallback != null) {
            return fallback.toCharSequence(minecraft);
        } else {
            Language language = Language.getInstance();
            if (this.cachedTooltip == null || language != this.splitWithLanguage) {
                List<FormattedCharSequence> tooltip = new ArrayList<>(splitTooltip(minecraft, this.message));
                tooltip.add(Component.empty().getVisualOrderText());
                tooltip.add(Component.translatable("message.charta.original", Component.literal(original).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED).getVisualOrderText());
                tooltip.add(Component.translatable("message.charta.current", Component.literal(current).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED).getVisualOrderText());
                this.cachedTooltip = tooltip;
                this.splitWithLanguage = language;
            }

            return this.cachedTooltip;
        }
    }
}
