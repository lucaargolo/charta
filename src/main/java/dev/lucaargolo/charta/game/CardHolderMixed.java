package dev.lucaargolo.charta.game;

import java.util.List;

public interface CardHolderMixed extends CardHolder {

    List<Card> charta_getHand();
    void charta_handUpdated();

    default List<Card> getHand() {
        return charta_getHand();
    }

    @Override
    default void handUpdated() {
        charta_handUpdated();
    }


}
