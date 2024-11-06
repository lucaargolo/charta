package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CrazyEightsGame implements CardGame<CrazyEightsGame> {

    private final Map<CardPlayer, List<Card>> censoredHands = new HashMap<>();

    private final List<Card> deck;
    private final List<CardPlayer> players;

    private final LinkedList<Card> playPile;
    private final LinkedList<Card> drawPile;

    private CardPlayer current;
    private CardPlayer winner;
    private boolean isGameOver;

    public int drawsLeft = 3;

    public CrazyEightsGame(List<CardPlayer> players, CardDeck deck) {
        this.players = players;

        this.deck = deck.getCards()
            .stream()
            .filter(c -> c.getSuit() != Card.Suit.BLANK && c.getRank() != Card.Rank.BLANK && c.getRank() != Card.Rank.JOKER)
            .map(Card::copy)
            .collect(Collectors.toList());
        this.deck.forEach(Card::flip);

        this.drawPile = new LinkedList<>();
        this.playPile = new LinkedList<>();
    }

    @Override
    public AbstractCardMenu<CrazyEightsGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new CrazyEightsMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
    }

    @Override
    public List<Card> getValidDeck() {
        List<Card> necessaryCards = new ArrayList<>();
        for(Card.Suit suit : Card.Suit.values()) {
            for(Card.Rank rank : Card.Rank.values()) {
                if(suit != Card.Suit.BLANK && rank != Card.Rank.BLANK && rank != Card.Rank.JOKER) {
                    necessaryCards.add(new Card(suit, rank));
                }
            }
        }
        return necessaryCards;
    }

    public LinkedList<Card> getPlayPile() {
        return playPile;
    }

    public LinkedList<Card> getDrawPile() {
        return drawPile;
    }

    @Override
    public List<CardPlayer> getPlayers() {
        return players;
    }

    @Override
    public List<Card> getCensoredHand(CardPlayer player) {
        return censoredHands.computeIfAbsent(player, p -> p.getHand().stream().map(c -> Card.BLANK).collect(Collectors.toList()));
    }

    @Override
    public CardPlayer getCurrentPlayer() {
        return current;
    }

    @Override
    public void setCurrentPlayer(int index) {
        this.current = getPlayers().get(index);
    }

    @Override
    public CardPlayer getNextPlayer() {
        if(current == null) {
            return getPlayers().getFirst();
        }else{
            int indexOf = getPlayers().indexOf(current);
            return getPlayers().get((indexOf + 1) % players.size());
        }
    }

    @Override
    public void startGame() {
        drawPile.clear();
        playPile.clear();

        drawPile.addAll(deck);
        Collections.shuffle(drawPile);

        for (CardPlayer player : players) {
            player.setPlay(new CompletableFuture<>());
            player.getHand().clear();
            getCensoredHand(player).clear();
            CardGame.dealCards(drawPile, player, getCensoredHand(player), 5);
            player.handUpdated();
        }

        Card last = drawPile.pollLast();
        last.flip();
        playPile.addLast(last);

        current = players.getFirst();

        winner = null;
        isGameOver = false;
    }

    @Override
    public void runGame() {
        if(drawPile.isEmpty()) {
            if(playPile.size() > 1) {
                Card lastCard = playPile.pollLast();
                playPile.forEach(Card::flip);
                drawPile.addAll(playPile);
                Collections.shuffle(drawPile);
                playPile.clear();
                playPile.add(lastCard);
            }else{
                endGame();
            }
        }

        current.getPlay(this).thenAccept(card -> {
            current.setPlay(new CompletableFuture<>());
            if(card == null) {
                if(drawsLeft > 0) {
                    drawsLeft--;
                    if(current.shouldCompute()) {
                        CardGame.dealCards(drawPile, current, getCensoredHand(current), 1);
                        current.handUpdated();
                    }
                    runGame();
                }else{
                    current = getNextPlayer();
                    drawsLeft = 3;
                    runGame();
                }
            }else if(canPlayCard(current, card)) {
                if(current.shouldCompute() && current.getHand().remove(card)) {
                    getCensoredHand(current).removeLast();
                    playPile.addLast(card);
                    current.handUpdated();
                }
                if(current.getHand().isEmpty()) {
                    endGame();
                }else {
                    current = getNextPlayer();
                    drawsLeft = 3;
                    runGame();
                }
            }
        });
    }

    @Override
    public void endGame() {
        if(current.getHand().isEmpty()) {
            winner = current;
        }
        isGameOver = true;
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        assert lastCard != null;
        return card.getRank() == lastCard.getRank() || card.getSuit() == lastCard.getSuit();
    }

    @Nullable
    @Override
    public Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    @Override
    public CardPlayer getWinner() {
        return winner;
    }

}
