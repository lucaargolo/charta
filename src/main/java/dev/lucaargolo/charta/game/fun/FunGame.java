package dev.lucaargolo.charta.game.fun;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.item.CardDeckItem;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.network.LastFunPayload;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class FunGame extends CardGame<FunGame> {

    public static final Set<Suit> SUITS = Set.of(Suit.RED, Suit.YELLOW, Suit.GREEN, Suit.BLUE);
    public static final Set<Rank> RANKS = Set.of(Rank.ONE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.ZERO, Rank.BLOCK, Rank.REVERSE, Rank.PLUS_2, Rank.WILD, Rank.WILD_PLUS_4);

    public static final int LAST_COOLDOWN = 30;

    private final GameOption.Number LAST_DRAW_AMOUNT = new GameOption.Number(2, 0, 6, Component.translatable("rule.charta.last_draw_amount"), Component.translatable("rule.charta.last_draw_amount.description"));
    private final GameOption.Bool STACK_ANY_PLUS2_ON_PLUS2 = new GameOption.Bool(true, Component.translatable("rule.charta.stack_any_plus2_on_plus2"), Component.translatable("rule.charta.stack_any_plus2_on_plus2.description"));
    private final GameOption.Bool STACK_SAME_PLUS2_ON_PLUS2 = new GameOption.Bool(true, Component.translatable("rule.charta.stack_same_plus2_on_plus2"), Component.translatable("rule.charta.stack_same_plus2_on_plus2.description"));
    private final GameOption.Bool STACK_PLUS4_ON_PLUS2 = new GameOption.Bool(true, Component.translatable("rule.charta.stack_plus4_on_plus2"), Component.translatable("rule.charta.stack_plus4_on_plus2.description"));
    private final GameOption.Bool STACK_PLUS4_ON_PLUS4 = new GameOption.Bool(false, Component.translatable("rule.charta.stack_plus4_on_plus4"), Component.translatable("rule.charta.stack_plus4_on_plus4.description"));
    private final GameOption.Bool STACK_SAME_PLUS2_ON_PLUS4 = new GameOption.Bool(false, Component.translatable("rule.charta.stack_same_plus2_on_plus4"), Component.translatable("rule.charta.stack_same_plus2_on_plus4.description"));
    private final GameOption.Bool STACK_ANY_PLUS2_ON_PLUS4 = new GameOption.Bool(false, Component.translatable("rule.charta.stack_any_plus2_on_plus4"), Component.translatable("rule.charta.stack_any_plus2_on_plus4.description"));

    private final PlaySlot playPile;
    private final DrawSlot drawPile;

    public final GameSlot suits = new GameSlot() {
        @Override
        public boolean removeAll() {
            return false;
        }
    };

    public boolean isChoosingWild;
    public Suit currentSuit;

    private final boolean[] saidLast;
    private final int[] lastCooldown;

    public boolean canDraw = true;
    public int drawStack = 0;
    public boolean startedDraw = false;
    public boolean reversed = false;

    private final Random random = new Random();

    public FunGame(List<CardPlayer> players, CardDeck deck) {
        super(players, deck);

        this.saidLast = new boolean[players.size()];
        this.lastCooldown = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            this.saidLast[i] = false;
            this.lastCooldown[i] = 0;
        }

        this.drawPile = addSlot(new DrawSlot(this, new LinkedList<>(), CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f - 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0, () -> this.canDraw));
        this.playPile = addSlot(new PlaySlot(this, new LinkedList<>(), CardTableBlockEntity.TABLE_WIDTH/2f - CardImage.WIDTH/2f + 20f, CardTableBlockEntity.TABLE_HEIGHT/2f - CardImage.HEIGHT/2f, 0, 0, this.drawPile));
    }

    @Override
    public GameSlot getPlayerHand(CardPlayer player) {
        return (player == this.getCurrentPlayer() && this.isChoosingWild) ? this.suits : super.getPlayerHand(player);
    }

    @Override
    protected GameSlot createPlayerHand(CardPlayer player) {
        return new GameSlot(player.hand()) {
            @Override
            public void onInsert(CardPlayer player, List<Card> cards) {
                super.onInsert(player, cards);
                if(drawPile.isDraw()) {
                    player.play(null);
                    drawPile.setDraw(false);
                }
            }

            @Override
            public boolean removeAll() {
                return false;
            }
        };
    }

    @Override
    public List<GameOption<?>> getOptions() {
        return List.of(
            LAST_DRAW_AMOUNT,
            STACK_ANY_PLUS2_ON_PLUS2,
            STACK_SAME_PLUS2_ON_PLUS2,
            STACK_PLUS4_ON_PLUS2,
            STACK_PLUS4_ON_PLUS4,
            STACK_SAME_PLUS2_ON_PLUS4,
            STACK_ANY_PLUS2_ON_PLUS4
        );
    }

    @Override
    public AbstractCardMenu<FunGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new FunMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray(), this.getRawOptions());
    }

    @Override
    public Predicate<CardDeck> getDeckPredicate() {
        return (deck) -> {
            return deck.getCards().size() >= 108 && SUITS.containsAll(deck.getUniqueSuits()) && deck.getUniqueSuits().containsAll(SUITS);
        };
    }

    @Override
    public Predicate<Card> getCardPredicate() {
        return (card) -> SUITS.contains(card.suit()) && RANKS.contains(card.rank());
    }

    @Override
    public void startGame() {
        drawPile.clear();
        playPile.clear();

        drawPile.addAll(gameDeck);
        drawPile.shuffle();

        for (CardPlayer player : players) {
            player.resetPlay();
            this.getPlayerHand(player).clear();
            this.getCensoredHand(player).clear();
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

        Card last = drawPile.removeLast();
        while (last != null && (last.rank() == Rank.WILD || last.rank() == Rank.JACK || last.rank() == Rank.QUEEN || last.rank() == Rank.PLUS_2 || last.rank() == Rank.WILD_PLUS_4)) {
            drawPile.add(last);
            drawPile.shuffle();
            last = drawPile.removeLast();
        }
        assert last != null;
        last.flip();
        Card startingCard = last;
        scheduledActions.add(() -> {
            playPile.addLast(startingCard);
            currentSuit = startingCard.suit();
        });

        currentPlayer = getNextPlayer();
        isChoosingWild = false;
        isGameReady = false;
        isGameOver = false;

        table(Component.translatable("message.charta.game_started"));
        table(Component.translatable("message.charta.its_player_turn", currentPlayer.getColoredName()));
    }

    private void shufflePiles() {
        Card lastCard = playPile.removeLast();
        playPile.forEach(Card::flip);
        drawPile.addAll(playPile);
        drawPile.shuffle();
        playPile.clear();
        playPile.add(lastCard);
        table(Component.translatable("message.charta.piles_shuffled"));
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

        currentPlayer.afterPlay(play -> {
            //Setup next play.
            currentPlayer.resetPlay();

            if(play == null) {
                //Player tried drawing a card.

                boolean canPlay = false;

                if (currentPlayer.shouldCompute()) {
                    dealCards(drawPile, currentPlayer, 1);
                    currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                }
                play(currentPlayer, Component.translatable("message.charta.drew_a_card"));

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
                    canPlay = this.getBestPlay(currentPlayer) != null;
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
            }else if(!currentPlayer.shouldCompute() || canPlay(currentPlayer, play)) {
                //Player played a card (Since we already checked in the menu, we don't need to check again if the player is pre computed).]
                Card card = play.cards().getLast();
                currentPlayer.playSound(ModSounds.CARD_PLAY.get());
                currentSuit = card.suit();

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
                if(currentPlayer.shouldCompute() && this.getPlayerHand(currentPlayer).remove(card)) {
                    this.getCensoredHand(currentPlayer).removeLast();
                    playPile.addLast(card);
                }

                if(getFullHand(currentPlayer).findAny().isEmpty()) {
                    //If the player hand is empty, they win!
                    endGame();
                }else if(card.rank() == Rank.WILD || card.rank() == Rank.WILD_PLUS_4) {
                    //If they played a wild card (Blank) or a wild plus four (Joker) we need to set up the suit choosing logic.

                    if(card.rank() == Rank.WILD_PLUS_4) {
                        //If they played a wild plus four (Joker), add 4 to the next player draw stack.
                        drawStack += 4;
                    }

                    if(currentPlayer.shouldCompute()) {
                        //If the player is a bot, we need to manually select the most frequent suit of that bot.
                        currentSuit = getMostFrequentSuit(currentPlayer);
                        play(currentPlayer, Component.translatable("message.charta.chose_a_suit", Component.translatable(deck.getSuitTranslatableKey(currentSuit))));
                        nextPlayerAndRunGame();
                    }else{
                        //If the player is not a bot, we need to set the game state as choosing wild, and set up the suits hand for the player.
                        isChoosingWild = true;
                        suits.clear();
                        suits.addAll(gameSuits.stream().map(s -> new Card(s, Rank.ZERO)).toList());
                        //They also can't draw during the suit choosing phase, so that's important.
                        canDraw = false;
                        runGame();
                    }
                } else {
                    //If the player did a regular play.

                    if(card.rank() == Rank.BLOCK) {
                        //If the play was a block, we skip the player manually once, so it ends up happening twice.
                        currentPlayer = getNextPlayer();
                        table(Component.translatable("message.charta.player_was_skipped", currentPlayer.getColoredName()));
                    }else if(card.rank() == Rank.REVERSE) {
                        //If the play was a reverse, we reverse the game order.
                        reversed = !reversed;
                        if(players.size() == 2) {
                            //If there are only two players, we need to skip the next one so the reverse actually does something (It acts as a block)
                            currentPlayer = getNextPlayer();
                        }
                        table(Component.translatable("message.charta.game_reversed"));
                    }else if(card.rank() == Rank.PLUS_2) {
                        //If the play was a plus 2, we add 2 to the next player draw stack
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
            table(Component.translatable("message.charta.its_player_turn_draw_stack", currentPlayer.getColoredName(), drawStack));
        }else{
            table(Component.translatable("message.charta.its_player_turn", currentPlayer.getColoredName()));
        }
        runGame();
    }

    @Override
    public void endGame() {
        if(getFullHand(currentPlayer).findAny().isEmpty()) {
            currentPlayer.sendTitle(Component.translatable("message.charta.you_won").withStyle(ChatFormatting.GREEN), Component.translatable("message.charta.congratulations"));
            getPlayers().stream().filter(player -> player != currentPlayer).forEach(player -> player.sendTitle(Component.translatable("message.charta.you_lost").withStyle(ChatFormatting.RED), Component.translatable("message.charta.won_the_match",currentPlayer.getName())));
        }else{
            getPlayers().forEach(player -> {
                player.sendTitle(Component.translatable("message.charta.draw").withStyle(ChatFormatting.YELLOW), Component.translatable("message.charta.no_winner"));
                player.sendMessage(Component.translatable("message.charta.match_unable").withStyle(ChatFormatting.GOLD));
            });
        }
        isGameOver = true;
    }

    @Override
    public void tick() {
        super.tick();
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
    public boolean canPlay(CardPlayer player, CardPlay play) {
        List<Card> cards = play.cards();
        if(cards.size() != 1) {
            return false;
        }
        Card card = cards.getLast();
        Card lastCard = playPile.getLast();

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
                boolean isPlus4 = lastCard.rank() == Rank.WILD_PLUS_4;
                if (isPlus4) {
                    return (STACK_PLUS4_ON_PLUS4.get() && card.rank() == Rank.WILD_PLUS_4) || (STACK_ANY_PLUS2_ON_PLUS4.get() && card.rank() == Rank.PLUS_2) || (STACK_SAME_PLUS2_ON_PLUS4.get() && card.rank() == Rank.PLUS_2 && card.suit() == currentSuit);
                } else {
                    return (STACK_PLUS4_ON_PLUS2.get() && card.rank() == Rank.WILD_PLUS_4) || (STACK_ANY_PLUS2_ON_PLUS2.get() && card.rank() == Rank.PLUS_2) || (STACK_SAME_PLUS2_ON_PLUS2.get() && card.rank() == Rank.PLUS_2 && card.suit() == currentSuit);
                }
            }
        }

        //If there aren't any edge cases, we check if the card is a wild card, or if it matches the current rank or suit.
        return card.rank() == Rank.WILD || card.rank() == Rank.WILD_PLUS_4 || card.rank() == lastCard.rank() || card.suit() == currentSuit;
    }

    public void sayLast(CardPlayer current) {
        //Someone said last! We need to check all the players to see if we can screw with someone.
        for(int i = 0; i < players.size(); i++) {
            CardPlayer player = players.get(i);
            if(didntSayLast(player)) {
                //Since player didn't say last yet, current is able to say it.
                table(Component.translatable("message.charta.player_said_last", current.getColoredName()));

                if(player == current) {
                    //Current is the player! So they can successfully say last to avoid drawing cards.
                    player.playSound(SoundEvents.NOTE_BLOCK_PLING.value());
                    //Set saidLast state to true, so you can't say last for this player again.
                    saidLast[i] = true;
                    lastCooldown[i] = 0;
                }else if(lastCooldown[i] > LAST_COOLDOWN) {
                    //Current is not the player! So we need to do a grace period.
                    //If the grace period already ended, they will automatically draw two cards.

                    int drawAmount = LAST_DRAW_AMOUNT.get();
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
                                PacketDistributor.sendToPlayer(serverPlayer, new LastFunPayload(CardDeckItem.getDeck(deck)));
                            }
                        });
                        table(Component.translatable("message.charta.player_automatically_drew_cards", player.getColoredName(), drawAmount));
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

}
