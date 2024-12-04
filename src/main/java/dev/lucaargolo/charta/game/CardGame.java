package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.network.CardPlayPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CardGame<G extends CardGame<G>> {

    public static CardPlayer TABLE = new AutoPlayer(0f) {
        @Override
        public Component getName() {
            return Component.empty();
        }
    };

    protected final Map<CardPlayer, GameSlot> censoredHands = new HashMap<>();
    protected final List<Runnable> scheduledActions = new ArrayList<>();

    private final List<GameSlot> gameSlots = new ArrayList<>();

    protected final List<CardPlayer> players;
    protected final CardDeck deck;

    protected final List<Card> gameDeck;
    protected final Set<Suit> gameSuits;

    protected CardPlayer currentPlayer;
    protected boolean isGameReady;
    protected boolean isGameOver;

    public CardGame(List<CardPlayer> players, CardDeck deck) {
        this.players = players;
        this.deck = deck;

        this.gameDeck = deck.getCards()
                .stream()
                .filter(c -> this.getCardPredicate().test(c))
                .map(Card::copy)
                .collect(Collectors.toList());
        this.gameDeck.forEach(Card::flip);
        this.gameSuits = this.gameDeck.stream().map(Card::getSuit).collect(Collectors.toSet());
    }

    public abstract AbstractCardMenu<G> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck);

    public abstract Predicate<CardDeck> getDeckPredicate();

    public abstract Predicate<Card> getCardPredicate();

    public abstract boolean canPlay(CardPlayer player, CardPlay play);

    public abstract void startGame();

    public abstract void runGame();

    public abstract void endGame();

    public abstract List<GameOption<?>> getOptions();

    public final byte[] getRawOptions() {
        List<GameOption<?>> options = this.getOptions();
        byte[] byteArray = new byte[options.size()];

        for (int i = 0; i < options.size(); i++) {
            byteArray[i] = options.get(i).getValue();
        }

        return byteArray;
    }

    public void setRawOptions(byte[] options) {
        List<GameOption<?>> o = this.getOptions();
        for(int i = 0; i < o.size(); i++) {
            if(i < options.length) {
                o.get(i).setValue(options[i]);
            }
        }
    }

    public List<CardPlayer> getPlayers() {
        return players;
    }

    public List<GameSlot> getSlots() {
        return gameSlots;
    }


    public GameSlot getSlot(int index) {
        return gameSlots.get(index);
    }

    protected GameSlot addSlot(GameSlot slot) {
        slot.setIndex(gameSlots.size());
        gameSlots.add(slot);
        return slot;
    }

    public boolean isGameReady() {
        return isGameReady;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public GameSlot getCensoredHand(CardPlayer player) {
        return getCensoredHand(null, player);
    }

    public GameSlot getCensoredHand(@Nullable CardPlayer viewer, CardPlayer player) {
        if(viewer == player && player.getEntity() instanceof ServerPlayer) {
            return player.getHand();
        }
        return censoredHands.computeIfAbsent(player, p -> {
            LinkedList<Card> list = new LinkedList<>();
            p.getHand().stream().map(c -> Card.BLANK).forEach(list::add);
            return new GameSlot(list);
        });
    }

    public CardPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int index) {
        this.currentPlayer = getPlayers().get(index);
    }

    protected Stream<Card> getFullHand(CardPlayer player) {
        LivingEntity entity = player.getEntity();
        if(entity instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof AbstractCardMenu<?> menu && !menu.getCarriedCards().isEmpty()) {
            return Stream.concat(player.getHand().stream(), menu.getCarriedCards().stream());
        }else{
            return player.getHand().stream();
        }
    }

    protected Suit getMostFrequentSuit(CardPlayer player) {
        Map<Suit, Integer> suitCountMap = new HashMap<>();

        //Adds all suits to a map, and increases its value everytime it appears.
        for (Card c : player.getHand().getCards()) {
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

    public @Nullable CardPlay getBestPlay(CardPlayer player) {
        for(int i = 0; i < gameSlots.size(); i++) {
            int slot = i;
            Optional<Card> card = getFullHand(player).filter(c -> canPlay(player, new CardPlay(List.of(c), slot))).findFirst();
            if(card.isPresent()) {
                return new CardPlay(List.of(card.get()), slot);
            }
        }
        return null;
    }

    public void openScreen(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos, CardDeck deck) {
        serverPlayer.openMenu(new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return CardGame.this.createMenu(containerId, playerInventory, level, pos, deck);
            }
        }, buf -> {
            buf.writeBlockPos(pos);
            CardDeck.STREAM_CODEC.encode(buf, deck);
            buf.writeVarIntArray(getPlayers().stream().mapToInt(CardPlayer::getId).toArray());
            buf.writeByteArray(this.getRawOptions());
        });
    }

    public void tick() {
        if(!this.isGameReady) {
            if(!this.scheduledActions.isEmpty()) {
                this.scheduledActions.removeFirst().run();
            } else {
                this.isGameReady = true;
                runGame();
            }
        }else{
            getPlayers().forEach(p -> p.tick(this));
        }
    }

    public int getMinPlayers() {
        return 2;
    }

    public int getMaxPlayers() {
        return 8;
    }

    protected void dealCards(GameSlot drawSlot, CardPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawSlot.removeLast();
            card.flip();
            player.getHand().add(card);
            getCensoredHand(player).add(Card.BLANK);
        }
    }

    protected void table(Component play) {
        play(TABLE, play.copy().withStyle(ChatFormatting.GRAY));
    }

    protected void play(CardPlayer player, Component play) {
        for(CardPlayer p : this.getPlayers()) {
            LivingEntity entity = p.getEntity();
            if(entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new CardPlayPayload(player.getName().equals(Component.empty()) ? Component.empty() : player.getColoredName(), player.getHand().size(), play));
            }
        }
    }

    public static boolean canPlayGame(CardGame<?> cardGame, CardDeck cardDeck) {
        return cardGame.getDeckPredicate().test(cardDeck);
    }

}