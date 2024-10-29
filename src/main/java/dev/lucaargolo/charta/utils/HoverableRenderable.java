package dev.lucaargolo.charta.utils;

import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.Nullable;

public interface HoverableRenderable extends Renderable {

    default @Nullable HoverableRenderable getHoverable() {
        return null;
    }

    default boolean isHovered() {
        return false;
    }

}
