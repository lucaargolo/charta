package dev.lucaargolo.charta.game;

import java.util.Collection;

public interface CardHolder {

    Collection<Card> getHand();
    void handUpdated();

}
