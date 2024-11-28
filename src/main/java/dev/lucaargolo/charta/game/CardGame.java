package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.FunMenu;
import dev.lucaargolo.charta.network.CardPlayPayload;
import dev.lucaargolo.charta.utils.GameSlot;
import dev.lucaargolo.charta.utils.TransparentLinkedList;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface CardGame<G extends CardGame<G>> {

    CardPlayer TABLE = new AutoPlayer(0f) {
        @Override
        public Component getName() {
            return Component.literal("Table");
        }
    };

    List<GameSlot> getGameSlots();

    List<Card> getValidDeck();

    List<CardPlayer> getPlayers();

    TransparentLinkedList<Card> getCensoredHand(CardPlayer viewer, CardPlayer player);

    CardPlayer getCurrentPlayer();

    void setCurrentPlayer(int index);

    void startGame();

    void runGame();

    void endGame();

    boolean canPlayCard(CardPlayer player, Card card);

    boolean isGameReady();

    void setGameReady(boolean ready);

    boolean isGameOver();

    AbstractCardMenu<G> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck);

    List<Runnable> getScheduledActions();

    default TransparentLinkedList<Card> getCensoredHand(CardPlayer player) {
        return getCensoredHand(null, player);
    }

    default Stream<Card> getFullHand(CardPlayer player) {
        LivingEntity entity = player.getEntity();
        if(entity instanceof ServerPlayer serverPlayer && serverPlayer.containerMenu instanceof FunMenu menu && !menu.getCarriedCards().isEmpty()) {
            return Stream.concat(player.getHand().stream(), menu.getCarriedCards().stream());
        }else{
            return player.getHand().stream();
        }
    }

    default Suit getMostFrequentSuit(CardPlayer player) {
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

    default @Nullable Card getBestCard(CardPlayer player) {
        return getFullHand(player).filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
    }

    default void openScreen(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos, CardDeck deck) {
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
        });
    }

    default void tick() {
        if(!isGameReady()) {
            if(!getScheduledActions().isEmpty()) {
                getScheduledActions().removeFirst().run();
            } else {
                setGameReady(true);
                runGame();
            }
        }else{
            getPlayers().forEach(p -> p.tick(this));
        }
    }

    default int getMinPlayers() {
        return 2;
    }

    default int getMaxPlayers() {
        return 8;
    }

    default void dealCards(LinkedList<Card> drawPile, CardPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawPile.pollLast();
            card.flip();
            player.getHand().add(card);
            getCensoredHand(player).add(Card.BLANK);
        }
    }

    default void tablePlay(Component play) {
        cardPlay(TABLE, play);
    }

    default void cardPlay(CardPlayer player, Component play) {
        for(CardPlayer p : this.getPlayers()) {
            LivingEntity entity = p.getEntity();
            if(entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new CardPlayPayload(player.getName(), player.getHand().size(), play));
            }
        }
    }

    static boolean canPlayGame(CardGame<?> cardGame, CardDeck cardDeck) {
        List<Card> necessaryCards = cardGame.getValidDeck();
        for(Card card : cardDeck.getCards()) {
            if(!necessaryCards.remove(card)) {
                necessaryCards.add(card);
                break;
            }
        }
        return necessaryCards.isEmpty();
    }

}