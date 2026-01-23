package dev.lucaargolo.charta.common.game.api.card;

import dev.lucaargolo.charta.common.game.Suits;
import org.jetbrains.annotations.NotNull;

public class Suit implements Comparable<Suit> {

    private int id = -1;

    @Override
    public int compareTo(@NotNull Suit other) {
        return this.id() - other.id();
    }

    private int id() {
        if(this.id == -1) {
            this.id = Suits.MOD_REGISTRY.getRegistry().getId(this);
        }
        return this.id;
    }

}
