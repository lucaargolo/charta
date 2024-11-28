package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.item.CardDeckItem;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.FunMenu;
import dev.lucaargolo.charta.network.LastFunPayload;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import dev.lucaargolo.charta.utils.GameSlot;
import dev.lucaargolo.charta.utils.TransparentLinkedList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FunGame implements CardGame<FunGame> {

    public static final int LAST_COOLDOWN = 30;

    //Rules
    public static final int STACK_ANY_PLUS2_ON_PLUS2 = 0;
    public static final int STACK_SAME_COLOR_PLUS2_ON_PLUS2 = 1;
    public static final int STACK_PLUS4_ON_PLUS2 = 2;
    public static final int STACK_PLUS4_ON_PLUS4 = 3;
    public static final int STACK_SAME_COLOR_PLUS2_ON_PLUS4 = 4;
    public static final int STACK_ANY_PLUS2_ON_PLUS4 = 5;

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

    private final boolean[] saidLast;
    private final int[] lastCooldown;

    public boolean canDraw = true;
    public int drawStack = 0;
    public boolean startedDraw = false;
    public boolean reversed = false;
    public int rules;

    private final Random random = new Random();

    public FunGame(List<CardPlayer> players, CardDeck deck) {
        this(players, deck, 0b000111);
    }

    public FunGame(List<CardPlayer> players, CardDeck deck, int rules) {
        this.players = players;
        this.saidLast = new boolean[players.size()];
        this.lastCooldown = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            this.saidLast[i] = false;
            this.lastCooldown[i] = 0;
        }

        this.deck = deck.getCards()
            .stream()
            .map(Card::copy)
            .collect(Collectors.toList());
        this.deck.forEach(Card::flip);

        this.cardDeck = deck;
        this.rules = rules;

        playPile.addConsumer(list -> {
            if(!list.isEmpty())
                tablePlay(Component.literal("Last card is "+cardDeck.getCardTranslatableKey(list.getLast())));
        });

        GameSlot playPileSlot = new GameSlot(playPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        GameSlot drawPileSlot = new GameSlot(drawPile, CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0);
        gameSlots.add(playPileSlot);
        gameSlots.add(drawPileSlot);
    }

    public boolean isRule(int rule) {
        return (rules & (1 << rule)) != 0;
    }

    @Override
    public AbstractCardMenu<FunGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new FunMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray());
    }

    @Override
    public List<GameSlot> getGameSlots() {
        return gameSlots;
    }

    @Override
    public List<Card> getValidDeck() {
        List<Card> necessaryCards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if(suit != Suit.BLANK) {
                for (Rank rank : Rank.values()) {
                    necessaryCards.add(new Card(suit, rank));
                    if(rank != Rank.BLANK && rank != Rank.JOKER && rank != Rank.TEN) {
                        necessaryCards.add(new Card(suit, rank));
                    }
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

        for(int i = 0; i < 7; i++) {
            for (CardPlayer player : players) {
                scheduledActions.add(() -> {
                    player.playSound(ModSounds.CARD_DRAW.get());
                    dealCards(drawPile, player, 1);
                });
                scheduledActions.add(() -> {});
            }
        }

        Card last = drawPile.pollLast();
        while (last != null && (last.getRank() == Rank.BLANK || last.getRank() == Rank.JACK || last.getRank() == Rank.QUEEN || last.getRank() == Rank.KING || last.getRank() == Rank.JOKER)) {
            drawPile.add(last);
            Collections.shuffle(drawPile, random);
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

        tablePlay(Component.literal("Game Started!"));
        tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
    }

    private void shufflePiles() {
        Card lastCard = playPile.pollLast();
        playPile.forEach(Card::flip);
        drawPile.addAll(playPile);
        Collections.shuffle(drawPile);
        playPile.clear();
        playPile.add(lastCard);
        tablePlay(Component.literal("Piles shuffled!"));
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
                shufflePiles();
            }else{
                endGame();
            }
        }

        currentPlayer.getPlay(this).thenAccept(card -> {
            //Setup next play.
            currentPlayer.setPlay(new CompletableFuture<>());

            if(card == null) {
                //Player tried drawing a card.

                boolean canPlay = false;

                if (currentPlayer.shouldCompute()) {
                    dealCards(drawPile, currentPlayer, 1);
                    currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                }
                cardPlay(currentPlayer, Component.literal("Player drew a card."));

                if(drawStack > 0) {
                    //They started drawing from a draw stack, so we need to block them from drawing.
                    startedDraw = true;
                    drawStack--;
                    //If the draw stack is empty, we need to set it so the player cant draw anymore.
                    canDraw = drawStack > 0;
                }else{
                    //They  did a regular draw. So they cant draw anymore.
                    canDraw = false;
                }
                //The player drew but not from a stack, so if they're able to, they should play.
                if(!startedDraw) {
                    canPlay = this.getBestCard(currentPlayer) != null;
                }
                //Since they drew a card, we reset their saidLast
                saidLast[getPlayers().indexOf(currentPlayer)] = false;

                if(!canDraw && !canPlay) {
                    //If the player can't draw anymore, and they cant play anymore, we skip to the next player.
                    startedDraw = false;
                    nextPlayerAndRunGame();
                }else{
                    //Continue game.
                    runGame();
                }
            }else if(!currentPlayer.shouldCompute() || canPlayCard(currentPlayer, card)) {
                //Player played a card (Since we already checked in the menu, we don't need to check again if the player is pre computed).
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.getSuit();

                if(isChoosingWild) {
                    //Player was choosing the suit from a wild card.
                    cardPlay(currentPlayer, Component.literal("Player chose "+cardDeck.getSuitTranslatableKey(currentSuit)));
                    //If the player was not a bot, there will be an extra card in the play pile, so we need to remove it.
                    if(!currentPlayer.shouldCompute()) {
                        playPile.removeLast();
                    }
                    isChoosingWild = false;
                }else{
                    cardPlay(currentPlayer, Component.literal("Player played a "+cardDeck.getCardTranslatableKey(card)));
                }

                //If the player is a bot, we need to manually remove the card from its hand and censored hand, and add it to the play pile.
                if(currentPlayer.shouldCompute() && currentPlayer.getHand().remove(card)) {
                    getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                }

                if(getFullHand(currentPlayer).findAny().isEmpty()) {
                    //If the player hand is empty, they win!
                    endGame();
                }else if(card.getRank() == Rank.BLANK || card.getRank() == Rank.JOKER) {
                    //If they played a wild card (Blank) or a wild plus four (Joker) we need to set up the suit choosing logic.

                    if(card.getRank() == Rank.JOKER) {
                        //If they played a wild plus four (Joker), add 4 to the next player draw stack.
                        drawStack += 4;
                    }

                    if(currentPlayer.shouldCompute()) {
                        //If the player is a bot, we need to manually select the most frequent suit of that bot.
                        currentSuit = getMostFrequentSuit(currentPlayer);
                        cardPlay(currentPlayer, Component.literal("Player chose "+cardDeck.getSuitTranslatableKey(currentSuit)));
                        nextPlayerAndRunGame();
                    }else{
                        //If the player is not a bot, we need to set the game state as choosing wild, and set up the suits hand for the player.
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(List.of(
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.HEARTS, Rank.TWO),
                                new Card(Suit.CLUBS, Rank.THREE),
                                new Card(Suit.DIAMONDS, Rank.FOUR)
                        ));
                        //They also can't draw during the suit choosing phase, so that's important.
                        canDraw = false;
                        runGame();
                    }
                } else {
                    //If the player did a regular play.

                    if(card.getRank() == Rank.JACK) {
                        //If the play was a block (Jack), we skip the player manually once, so it ends up happening twice.
                        currentPlayer = getNextPlayer();
                        tablePlay(Component.literal(currentPlayer.getName().getString()+" was skipped."));
                    }else if(card.getRank() == Rank.QUEEN) {
                        //If the play was a reverse (Queen), we reverse the game order.
                        reversed = !reversed;
                        if(players.size() == 2) {
                            //If there are only two players, we need to skip the next one so the reverse actually does something (It acts as a block)
                            currentPlayer = getNextPlayer();
                        }
                        tablePlay(Component.literal("Game direction was reversed."));
                    }else if(card.getRank() == Rank.KING) {
                        //If the play was a plus 2 (King), we add 2 to the next player draw stack
                        drawStack += 2;
                    }

                    nextPlayerAndRunGame();
                }
            }
        });
    }

    private void nextPlayerAndRunGame() {
        //Reset states and go to the next player;
        canDraw = true;
        currentPlayer = getNextPlayer();
        if(drawStack > 0) {
            tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn. They need to draw "+drawStack+" cards."));
        }else{
            tablePlay(Component.literal("Its "+currentPlayer.getName().getString()+"'s turn"));
        }
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
    public void tick() {
        CardGame.super.tick();
        if(isGameReady) {
            //Here we do the logic to increase the grace period if the player can say last. Enable bots to say last for themselves and for other players.
            for(int i = 0; i < this.getPlayers().size(); i++) {
                CardPlayer player = this.getPlayers().get(i);
                boolean didntSayLast = didntSayLast(player);
                if(player.shouldCompute()) {
                    if(didntSayLast && lastCooldown[i] % (LAST_COOLDOWN/2) == 0 && random.nextBoolean()) {
                        sayLast(player);
                    }
                    for(int j = 0; j < this.getPlayers().size(); j++) {
                        CardPlayer other = this.getPlayers().get(j);
                        if(lastCooldown[j] > LAST_COOLDOWN && random.nextBoolean() && didntSayLast(other)) {
                            sayLast(player);
                        }
                    }
                }
                if(didntSayLast) {
                    lastCooldown[i]++;
                }
            }
        }
    }

    @Override
    public boolean canPlayCard(CardPlayer player, Card card) {
        Card lastCard = playPile.peekLast();

        if(!isGameReady || lastCard == null) {
            //Check if game is ready.
            return false;
        }
        if(isChoosingWild) {
            //If player is choosing wild. They can play anything.
            return true;
        }

        if(drawStack > 0) {
            //If draw stack is greater than one, we need to check a few conditions based on the current rules.
            if(startedDraw) {
                //If the player already started drawing, they can't stop anymore.
                return false;
            }else {
                //If the player didn't start drawing, they can stack another plus card if the rules allow it.
                boolean isPlus4 = lastCard.getRank() == Rank.JOKER;
                if (isPlus4) {
                    return (isRule(STACK_PLUS4_ON_PLUS4) && card.getRank() == Rank.JOKER) || (isRule(STACK_ANY_PLUS2_ON_PLUS4) && card.getRank() == Rank.KING) || (isRule(STACK_SAME_COLOR_PLUS2_ON_PLUS4) && card.getRank() == Rank.KING && card.getSuit() == currentSuit);
                } else {
                    return (isRule(STACK_PLUS4_ON_PLUS2) && card.getRank() == Rank.JOKER) || (isRule(STACK_ANY_PLUS2_ON_PLUS2) && card.getRank() == Rank.KING) || (isRule(STACK_SAME_COLOR_PLUS2_ON_PLUS2) && card.getRank() == Rank.KING && card.getSuit() == currentSuit);
                }
            }
        }

        //If there aren't any edge cases, we check if the card is a wild card, or if it matches the current rank or suit.
        return card.getRank() == Rank.BLANK || card.getRank() == Rank.JOKER || card.getRank() == lastCard.getRank() || card.getSuit() == currentSuit;
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

    public void sayLast(CardPlayer current) {
        //Someone said last! We need to check all the players to see if we can screw with someone.
        for(int i = 0; i < players.size(); i++) {
            CardPlayer player = players.get(i);
            if(didntSayLast(player)) {
                //Since player didn't say last yet, current is able to say it.
                tablePlay(Component.literal(current.getName().getString()+" said Last!"));

                if(player == current) {
                    //Current is the player! So they can successfully say last to avoid drawing cards.
                    player.playSound(SoundEvents.NOTE_BLOCK_PLING.value());
                    //Set saidLast state to true, so you can't say last for this player again.
                    saidLast[i] = true;
                    lastCooldown[i] = 0;
                }else if(lastCooldown[i] > LAST_COOLDOWN) {
                    //Current is not the player! So we need to do a grace period.
                    //If the grace period already ended, they will automatically draw two cards.

                    int drawAmount = 2;
                    //First we need to check if there are enough cards to draw.
                    if(drawPile.size() < drawAmount) {
                        //If there isn't. We shuffle the piles.
                        shufflePiles();
                    }
                    //We check again if there are enough cards to draw.
                    if(drawPile.size() >= drawAmount) {
                        //If there is, we sent a totem animation to show someone forget to say last.
                        //And add the cards to this player.
                        players.forEach(p -> {
                            LivingEntity entity = p.getEntity();
                            if(entity instanceof ServerPlayer serverPlayer) {
                                PacketDistributor.sendToPlayer(serverPlayer, new LastFunPayload(CardDeckItem.getDeck(cardDeck)));
                            }
                        });
                        tablePlay(Component.literal(player.getName().getString()+" automatically drew "+drawAmount+" cards"));
                        dealCards(drawPile, player, drawAmount);
                    }else{
                        //If there isn't, we end the game due to lack of cards.
                        endGame();
                    }

                    //Set saidLast state to true, so you can't say last for this player again.
                    saidLast[i] = true;
                    lastCooldown[i] = 0;
                }
            }
        }
    }

    public boolean canDoLast() {
        //Checks if it's possible to say Last for all players!
        return isGameReady && getPlayers().stream().anyMatch(this::didntSayLast);
    }

    public boolean didntSayLast(CardPlayer player) {
        //Checks if it's possible to say Last for a player!
        int index = this.getPlayers().indexOf(player);
        if(this.saidLast[index]) {
            //This player already said last, so you cant say it again
            return false;
        }else{
            //This player didn't say last yet, so we need to check its hand size.
            int size = (int) getFullHand(player).count();
            //Return true if size is equal to one.
            return size == 1;
        }
    }

    private CardPlayer getNextPlayer() {
        if(currentPlayer == null) {
            //If current player is null, just get the first one.
            return getPlayers().getFirst();
        }else{
            //If current player is not null, we need to check who is the next player based on the order of the game.
            int indexOf = getPlayers().indexOf(currentPlayer);
            if(reversed) {
                //If the game is reversed. We need to do index - 1.
                if(indexOf - 1 >= 0) {
                    //If index - 1 is positive, we just get the player using that.
                    return getPlayers().get(indexOf - 1);
                }else{
                    //If index - 1 is negative, we get the last player in the list.
                    return getPlayers().getLast();
                }
            }else{
                //If the game is not reversed. We just get the next index.
                return getPlayers().get((indexOf + 1) % players.size());
            }
        }
    }

    private Suit getMostFrequentSuit(CardPlayer player) {
        Map<Suit, Integer> suitCountMap = new HashMap<>();

        //Adds all suits to a map, and increases its value everytime it appears.
        for (Card c : player.getHand()) {
            Suit suit = c.getSuit();
            suitCountMap.put(suit, suitCountMap.getOrDefault(suit, 0) + 1);
        }

        Suit mostFrequentSuit = null;
        int maxCount = 0;

        //Check what suit appears the most.
        for (Map.Entry<Suit, Integer> entry : suitCountMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostFrequentSuit = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return mostFrequentSuit;
    }


}
