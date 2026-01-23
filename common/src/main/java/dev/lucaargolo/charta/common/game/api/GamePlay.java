package dev.lucaargolo.charta.common.game.api;

import dev.lucaargolo.charta.common.game.api.card.Card;

import java.util.List;

public record GamePlay(List<Card> cards, int slot) {
}
