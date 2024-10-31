package dev.lucaargolo.charta.game;

import java.util.Collection;
import java.util.List;

public interface CardHolder {

    List<Card> getHand();
    void handUpdated();

}
