package dev.lucaargolo.charta.common.utils;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface HoverableRenderable extends Renderable {

    default @Nullable HoverableRenderable getHoverable() {
        return null;
    }

    default boolean isHovered() {
        return false;
    }

    void scheduleTooltip(Component component);

}
