package dev.lucaargolo.charta.game.solitaire;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class SolitaireGame extends CardGame<SolitaireGame> {

    private final GameSlot stockPile;
    private final GameSlot wastePile;

    private final Map<Suit, GameSlot> foundationPiles;
    private final List<GameSlot> tableauPiles;

    public SolitaireGame(List<CardPlayer> players, CardDeck deck) {
        super(players, deck);

        float middleX = CardTableBlockEntity.TABLE_WIDTH/2f;
        float leftX = middleX - CardImage.WIDTH/2f - (CardImage.WIDTH + 5)*3;

        float middleY = CardTableBlockEntity.TABLE_HEIGHT/2f;
        float topY = middleY + (1.75f * CardImage.HEIGHT);

        this.stockPile = addSlot(new GameSlot(new LinkedList<>(), leftX, topY, 0f, 0f));
        this.wastePile = addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + 5, topY, 0f, 0f));

        this.foundationPiles = Map.of(
            Suit.SPADES, addSlot(new GameSlot(new LinkedList<>(), leftX + (CardImage.WIDTH + 5)*3, topY, 0f, 0f)),
            Suit.HEARTS, addSlot(new GameSlot(new LinkedList<>(), leftX + (CardImage.WIDTH + 5)*4, topY, 0f, 0f)),
            Suit.CLUBS, addSlot(new GameSlot(new LinkedList<>(), leftX + (CardImage.WIDTH + 5)*5, topY, 0f, 0f)),
            Suit.DIAMONDS, addSlot(new GameSlot(new LinkedList<>(), leftX + (CardImage.WIDTH + 5)*6, topY, 0f, 0f))
        );

        this.tableauPiles = List.of(
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + CardImage.WIDTH + 5, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*2, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*3, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*4, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*5, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)),
            addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*6, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false))
        );
    }

    @Override
    public AbstractCardMenu<SolitaireGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new SolitaireMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray(), this.getRawOptions());
    }

    @Override
    public Predicate<CardDeck> getDeckPredicate() {
        return deck -> {
            for(Suit suit : Suit.values()) {
                for(Rank rank : Rank.values()) {
                    if(suit != Suit.BLANK && rank != Rank.BLANK && rank != Rank.JOKER && !deck.getCards().contains(new Card(suit, rank))) {
                        return false;
                    }
                }
            }
            return true;
        };
    }

    @Override
    public Predicate<Card> getCardPredicate() {
        return card -> card.getSuit() != Suit.BLANK && card.getRank() != Rank.BLANK && card.getRank() != Rank.JOKER;
    }

    @Override
    public boolean canPlay(CardPlayer player, CardPlay play) {
        return false;
    }

    @Override
    public void startGame() {
        this.stockPile.clear();
        this.wastePile.clear();

        this.foundationPiles.values().forEach(GameSlot::clear);
        this.tableauPiles.forEach(GameSlot::clear);

        this.stockPile.addAll(gameDeck);
        this.stockPile.shuffle();

        for (CardPlayer player : players) {
            player.setPlay(new CompletableFuture<>());
            player.getHand().clear();
            getCensoredHand(player).clear();
        }

        for (int i = 0; i < this.tableauPiles.size(); i++) {
            for (int j = 0; j <= i; j++) {
                int slot = i;
                int amount = j;
                this.scheduledActions.add(() -> {
                    this.currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                    Card card = this.stockPile.removeLast();
                    if(slot == amount) card.flip();
                    this.tableauPiles.get(slot).addLast(card);
                });
                this.scheduledActions.add(() -> {});
            }
        }

        this.currentPlayer = players.getFirst();
        this.isGameReady = false;
        this.isGameOver = false;

        table(Component.translatable("message.charta.game_started"));
    }

    @Override
    public void runGame() {

    }

    @Override
    public void endGame() {
        this.isGameOver = true;
    }

    @Override
    public int getMinPlayers() {
        return 1;
    }

    @Override
    public int getMaxPlayers() {
        return 1;
    }

    @Override
    public List<GameOption<?>> getOptions() {
        return List.of();
    }

    private boolean isAlternateColor(Card c1, Card c2) {
        boolean c1Red = (c1.getSuit() == Suit.HEARTS || c1.getSuit() == Suit.DIAMONDS);
        boolean c2Red = (c2.getSuit() == Suit.HEARTS || c2.getSuit() == Suit.DIAMONDS);
        return c1Red != c2Red;
    }
}
