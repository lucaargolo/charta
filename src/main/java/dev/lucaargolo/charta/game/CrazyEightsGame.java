package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CrazyEightsMenu;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.GameSlot;
import dev.lucaargolo.charta.utils.TransparentLinkedList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CrazyEightsGame implements CardGame<CrazyEightsGame> {

    private final Map<CardPlayer, TransparentLinkedList<Card>> censoredHands = new HashMap<>();

    private final TransparentLinkedList<Card> playPile = new TransparentLinkedList<>();
    private final TransparentLinkedList<Card> drawPile = new TransparentLinkedList<>();
    private final List<GameSlot> gameSlots = new ArrayList<>();

    private final List<Runnable> scheduledActions = new ArrayList<>();
    private boolean isGameReady;

    private final List<CardPlayer> players;
    private final List<Card> deck;
    private final CardDeck cardDeck;

    private CardPlayer currentPlayer;
    private boolean isGameOver;

    public final LinkedList<Card> suits = new LinkedList<>();
    public boolean isChoosingWild;
    public Suit currentSuit;
    public int drawsLeft = 3;

    public CrazyEightsGame(List<CardPlayer> players, CardDeck deck) {
        this.players = players;

        this.deck = deck.getCards()
            .stream()
            .filter(c -> c.getSuit() != Suit.BLANK && c.getRank() != Rank.BLANK && c.getRank() != Rank.JOKER)
            .map(Card::copy)
            .collect(Collectors.toList());
        this.deck.forEach(Card::flip);

        this.cardDeck = deck;

        GameSlot playPileSlot = new GameSlot(playPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        GameSlot drawPileSlot = new GameSlot(drawPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        gameSlots.add(playPileSlot);
        gameSlots.add(drawPileSlot);
    }

    @Override
    public AbstractCardMenu<CrazyEightsGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new CrazyEightsMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
    }

    @Override
    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

    @Override
    public List<Card> getValidDeck() {
        List<Card> necessaryCards = new ArrayList<>();
        for(Suit suit : Suit.values()) {
            for(Rank rank : Rank.values()) {
                if(suit != Suit.BLANK && rank != Rank.BLANK && rank != Rank.JOKER) {
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
    public TransparentLinkedList<Card> getCensoredHand(@Nullable CardPlayer viewer, CardPlayer player) {
        if(viewer == player && player.getEntity() instanceof ServerPlayer) {
            return player.getHand();
        }
        return censoredHands.computeIfAbsent(player, p -> {
            TransparentLinkedList<Card> list = new TransparentLinkedList<>();
            p.getHand().stream().map(c -> Card.BLANK).forEach(list::add);
            return list;
        });
    }

    @Override
    public CardPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public void setCurrentPlayer(int index) {
        this.currentPlayer = getPlayers().get(index);
    }

    private CardPlayer getNextPlayer() {
        if(currentPlayer == null) {
            return getPlayers().getFirst();
        }else{
            int indexOf = getPlayers().indexOf(currentPlayer);
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
        }

        for(int i = 0; i < 5; i++) {
            for (CardPlayer player : players) {
                scheduledActions.add(() -> {
                    player.playSound(ModSounds.CARD_DRAW.get());
                    dealCards(drawPile, player, 1);
                });
                scheduledActions.add(() -> {});
            }
        }

        Card last = drawPile.pollLast();
        while (last != null && last.getRank() == Rank.EIGHT) {
            drawPile.add(last);
            Collections.shuffle(drawPile);
            last = drawPile.pollLast();
        }
        assert last != null;
        last.flip();
        Card startingCard = last;
        scheduledActions.add(() -> {
            playPile.addLast(startingCard);
            currentSuit = startingCard.getSuit();
        });

        currentPlayer = getNextPlayer();
        isChoosingWild = false;
        isGameReady = false;
        isGameOver = false;

        table(Component.translatable("charta.message.game_started"));
        table(Component.translatable("charta.message.its_player_turn", currentPlayer.getColoredName()));
    }

    @Override
    public void runGame() {
        if(!isGameReady) {
            return;
        }

        //We check if the drawPile is empty.
        //If it is, we shuffle remove all cards except the last one from the play pile, add them to the draw pile, and shuffle them.
        //If there are no cards in the play pile either, we end the game due to lack of cards.
        if(drawPile.isEmpty()) {
            if(playPile.size() > 1) {
                Card lastCard = playPile.pollLast();
                playPile.forEach(Card::flip);
                drawPile.addAll(playPile);
                Collections.shuffle(drawPile);
                playPile.clear();
                playPile.add(lastCard);
                table(Component.translatable("charta.message.piles_shuffled"));
            }else{
                endGame();
            }
        }

        currentPlayer.getPlay(this).thenAccept(card -> {
            //Setup next play.
            currentPlayer.setPlay(new CompletableFuture<>());

            if(card == null) {
                //Player tried drawing a card.
                if(drawsLeft > 0) {
                    drawsLeft--;

                    play(currentPlayer, Component.translatable("charta.message.drew_a_card"));

                    if(currentPlayer.shouldCompute()) {
                        dealCards(drawPile, currentPlayer, 1);
                        currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                    }

                    if(drawsLeft == 0 && this.getBestCard(currentPlayer) == null) {
                        nextPlayerAndRunGame();
                    }else{
                        runGame();
                    }
                }else{
                    nextPlayerAndRunGame();
                }
            }else if(!currentPlayer.shouldCompute() || canPlayCard(currentPlayer, card)) {
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.getSuit();

                if(isChoosingWild) {
                    //Player was choosing the suit from a wild card.
                    play(currentPlayer, Component.translatable("charta.message.chose_a_suit", Component.translatable(cardDeck.getSuitTranslatableKey(currentSuit)).withColor(cardDeck.getSuitColor(currentSuit))));
                    //If the player was not a bot, there will be an extra card in the play pile, so we need to remove it.
                    if(!currentPlayer.shouldCompute()) {
                        playPile.removeLast();
                    }
                    isChoosingWild = false;
                }else{
                    play(currentPlayer, Component.translatable("charta.message.played_a_card", Component.translatable(cardDeck.getCardTranslatableKey(card)).withColor(cardDeck.getCardColor(card))));
                }

                //If the player is a bot, we need to manually remove the card from its hand and censored hand, and add it to the play pile.
                if(currentPlayer.shouldCompute() && currentPlayer.getHand().remove(card)) {
                    getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                }

                if(getFullHand(currentPlayer).findAny().isEmpty()) {
                    //If the player hand is empty, they win!
                    endGame();
                }else if(card.getRank() == Rank.EIGHT) {
                    //If they played a wild card (Eight) we need to set up the suit choosing logic.
                    if(currentPlayer.shouldCompute()) {
                        //If the player is a bot, we need to manually select the most frequent suit of that bot.
                        currentSuit = getMostFrequentSuit(currentPlayer);
                        play(currentPlayer, Component.translatable("charta.message.chose_a_suit", Component.translatable(cardDeck.getSuitTranslatableKey(currentSuit))));
                        nextPlayerAndRunGame();
                    }else{
                        //If the player is not a bot, we need to set the game state as choosing wild, and set up the suits hand for the player.
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(List.of(
                                new Card(Suit.SPADES, Rank.BLANK),
                                new Card(Suit.HEARTS, Rank.BLANK),
                                new Card(Suit.CLUBS, Rank.BLANK),
                                new Card(Suit.DIAMONDS, Rank.BLANK)
                        ));
                        //They also can't draw during the suit choosing phase, so that's important.
                        drawsLeft = 0;
                        runGame();
                    }
                } else {
                    //If the player did a regular play.
                    nextPlayerAndRunGame();
                }
            }
        });
    }

    public void nextPlayerAndRunGame() {
        drawsLeft = 3;
        currentPlayer = getNextPlayer();
        table(Component.translatable("charta.message.its_player_turn", currentPlayer.getColoredName()));
        runGame();
    }

    @Override
    public void endGame() {
        if(getFullHand(currentPlayer).findAny().isEmpty()) {
            currentPlayer.sendTitle(Component.translatable("charta.message.you_won").withStyle(ChatFormatting.GREEN), Component.translatable("charta.message.congratulations"));
            getPlayers().stream().filter(player -> player != currentPlayer).forEach(player -> {
                player.sendTitle(Component.translatable("charta.message.you_lost").withStyle(ChatFormatting.RED), Component.translatable("charta.message.won_the_match",currentPlayer.getName()));
            });
        }else{
            getPlayers().forEach(player -> {
                currentPlayer.sendTitle(Component.translatable("charta.message.draw").withStyle(ChatFormatting.YELLOW), Component.translatable("charta.message.no_winner"));
                player.sendMessage(Component.translatable("charta.message.match_unable").withStyle(ChatFormatting.GOLD));
            });
        }
        isGameOver = true;
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();
        return isGameReady && lastCard != null && ((isChoosingWild && card.getRank() == Rank.BLANK) || card.getRank() == Rank.EIGHT || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit);
    }

    @Override
    public boolean isGameReady() {
        return isGameReady;
    }

    @Override
    public void setGameReady(boolean gameReady) {
        isGameReady = gameReady;
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    @Override
    public List<Runnable> getScheduledActions() {
        return scheduledActions;
    }
}
