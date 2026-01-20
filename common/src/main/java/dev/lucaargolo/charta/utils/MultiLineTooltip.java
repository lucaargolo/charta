package dev.lucaargolo.charta.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiLineTooltip extends Tooltip {

    private final Component[] components;

    public MultiLineTooltip(Component... message) {
        super(message[0], message[0]);
        this.components = message;
    }

    @Override
    public @NotNull List<FormattedCharSequence> toCharSequence(@NotNull Minecraft minecraft) {
        Language language = Language.getInstance();
        if (this.cachedTooltip == null || language != this.splitWithLanguage) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            for(Component component : this.components) {
                List<FormattedCharSequence> split = splitTooltip(minecraft, component);
                lines.addAll(split);
                if(split.isEmpty()) {
                    lines.add(Component.empty().getVisualOrderText());
                }
            }

            this.cachedTooltip = lines;
            this.splitWithLanguage = language;
        }

        return this.cachedTooltip;
    }
}
