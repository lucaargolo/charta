package dev.lucaargolo.charta.client.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractPreciseWidget extends AbstractWidget {

    private float preciseX;
    private float preciseY;
    private float preciseWidth;
    private float preciseHeight;

    public AbstractPreciseWidget(float x, float y, float width, float height, Component message) {
        super(Mth.floor(x), Mth.floor(y), Mth.floor(width), Mth.floor(height), message);
        this.preciseX = x;
        this.preciseY = y;
        this.preciseWidth = width;
        this.preciseHeight = height;
    }

    public float getPreciseX() {
        return preciseX;
    }

    public void setPreciseX(float preciseX) {
        this.preciseX = preciseX;
        this.setX(Mth.floor(preciseX));
    }

    public float getPreciseY() {
        return preciseY;
    }

    public void setPreciseY(float preciseY) {
        this.preciseY = preciseY;
        this.setY(Mth.floor(preciseY));
    }

    public float getPreciseWidth() {
        return preciseWidth;
    }

    public void setPreciseWidth(float preciseWidth) {
        this.preciseWidth = preciseWidth;
        this.setWidth(Mth.floor(preciseWidth));
    }

    public float getPreciseHeight() {
        return preciseHeight;
    }

    public void setPreciseHeight(float preciseHeight) {
        this.preciseHeight = preciseHeight;
        this.setHeight(Mth.floor(preciseHeight));
    }

}
