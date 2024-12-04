package dev.lucaargolo.charta.game.crazyeights;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CrazyEightsGame extends CardGame<CrazyEightsGame> {

    private final GameOption.Number AVAILABLE_DRAWS = new GameOption.Number(3, 1, 5, Component.translatable("rule.charta.available_draws"), Component.translatable("rule.charta.available_draws.description"));

    private final GameSlot playPile;
    private final GameSlot drawPile;

    public final GameSlot suits = new GameSlot();
    public boolean isChoosingWild;
    public Suit currentSuit;
    public int drawsLeft;

    public CrazyEightsGame(List<CardPlayer> players, CardDeck deck) {
        super(players, deck);
        this.drawsLeft = AVAILABLE_DRAWS.get();

        this.drawPile = addSlot(new GameSlot(new LinkedList<>(), CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0));
        this.playPile = addSlot(new GameSlot(new LinkedList<>(), CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0));
    }

    @Override
    public List<GameOption<?>> getOptions() {
        return List.of(AVAILABLE_DRAWS);
    }

    @Override
    public AbstractCardMenu<CrazyEightsGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new CrazyEightsMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray(), this.getRawOptions());
    }

    @Override
    public Predicate<CardDeck> getDeckPredicate() {
        return (deck) -> deck.getCards().stream().filter(c -> c.getSuit() == Suit.BLANK || c.getRank() == Rank.BLANK).findAny().isEmpty();
    }

    @Override
    public Predicate<Card> getCardPredicate() {
        return (card) -> card.getRank() != Rank.JOKER;
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

        drawPile.addAll(gameDeck);
        drawPile.shuffle();

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

        Card last = drawPile.removeLast();
        while (last != null && last.getRank() == Rank.EIGHT) {
            drawPile.add(last);
            drawPile.shuffle();
            last = drawPile.removeLast();
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

        table(Component.translatable("message.charta.game_started"));
        table(Component.translatable("message.charta.its_player_turn", currentPlayer.getColoredName()));
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
                Card lastCard = playPile.removeLast();
                playPile.forEach(Card::flip);
                drawPile.addAll(playPile);
                drawPile.shuffle();
                playPile.clear();
                playPile.add(lastCard);
                table(Component.translatable("message.charta.piles_shuffled"));
            }else{
                endGame();
            }
        }

        currentPlayer.getPlay(this).thenAccept(play -> {
            //Setup next play.
            currentPlayer.setPlay(new CompletableFuture<>());

            if(play == null) {
                //Player tried drawing a card.
                if(drawsLeft > 0) {
                    drawsLeft--;

                    play(currentPlayer, Component.translatable("message.charta.drew_a_card"));

                    if(currentPlayer.shouldCompute()) {
                        dealCards(drawPile, currentPlayer, 1);
                        currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                    }

                    if(drawsLeft == 0 && this.getBestPlay(currentPlayer) == null) {
                        nextPlayerAndRunGame();
                    }else{
                        runGame();
                    }
                }else{
                    nextPlayerAndRunGame();
                }
            }else if(!currentPlayer.shouldCompute() || canPlay(currentPlayer, play)) {
                Card card = play.cards().getLast();
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.getSuit();

                if(isChoosingWild) {
                    //Player was choosing the suit from a wild card.
                    play(currentPlayer, Component.translatable("message.charta.chose_a_suit", Component.translatable(deck.getSuitTranslatableKey(currentSuit)).withColor(deck.getSuitColor(currentSuit))));
                    //If the player was not a bot, there will be an extra card in the play pile, so we need to remove it.
                    if(!currentPlayer.shouldCompute()) {
                        playPile.removeLast();
                    }
                    isChoosingWild = false;
                }else{
                    play(currentPlayer, Component.translatable("message.charta.played_a_card", Component.translatable(deck.getCardTranslatableKey(card)).withColor(deck.getCardColor(card))));
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
                        play(currentPlayer, Component.translatable("message.charta.chose_a_suit", Component.translatable(deck.getSuitTranslatableKey(currentSuit))));
                        nextPlayerAndRunGame();
                    }else{
                        //If the player is not a bot, we need to set the game state as choosing wild, and set up the suits hand for the player.
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(gameSuits.stream().map(s -> new Card(s, Rank.BLANK)).toList());
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
        drawsLeft = AVAILABLE_DRAWS.get();
        currentPlayer = getNextPlayer();
        table(Component.translatable("message.charta.its_player_turn", currentPlayer.getColoredName()));
        runGame();
    }

    @Override
    public void endGame() {
        if(getFullHand(currentPlayer).findAny().isEmpty()) {
            currentPlayer.sendTitle(Component.translatable("message.charta.you_won").withStyle(ChatFormatting.GREEN), Component.translatable("message.charta.congratulations"));
            getPlayers().stream().filter(player -> player != currentPlayer).forEach(player -> player.sendTitle(Component.translatable("message.charta.you_lost").withStyle(ChatFormatting.RED), Component.translatable("message.charta.won_the_match",currentPlayer.getName())));
        }else{
            getPlayers().forEach(player -> {
                currentPlayer.sendTitle(Component.translatable("message.charta.draw").withStyle(ChatFormatting.YELLOW), Component.translatable("message.charta.no_winner"));
                player.sendMessage(Component.translatable("message.charta.match_unable").withStyle(ChatFormatting.GOLD));
            });
        }
        isGameOver = true;
    }

    @Override
    public boolean canPlay(CardPlayer player, CardPlay play) {
        List<Card> cards = play.cards();
        if(cards.size() != 1) {
            return false;
        }
        Card card = cards.getLast();
        Card lastCard = playPile.getLast();
        return isGameReady && lastCard != null && ((isChoosingWild && card.getRank() == Rank.BLANK) || card.getRank() == Rank.EIGHT || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit);
    }

}
