package dev.lucaargolo.charta.game;

import java.util.Collection;

public interface CardHolderMixed extends CardHolder {

    Collection<Card> charta_getHand();
    void charta_handUpdated();

    default Collection<Card> getHand() {
        return charta_getHand();
    }

    @Override
    default void handUpdated() {
        charta_handUpdated();
    }


}
