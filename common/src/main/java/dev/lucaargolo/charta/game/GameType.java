package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;

import java.util.List;

@FunctionalInterface
public interface GameType<G extends Game<G, M>, M extends AbstractCardMenu<G, M>> {

    G create(List<CardPlayer> players, Deck deck);

}
