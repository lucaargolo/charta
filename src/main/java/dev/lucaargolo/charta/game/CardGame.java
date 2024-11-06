package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public interface CardGame<G extends CardGame<G>> {

    List<Card> getValidDeck();

    List<CardPlayer> getPlayers();

    List<Card> getCensoredHand(CardPlayer player);

    CardPlayer getCurrentPlayer();

    void setCurrentPlayer(int index);

    CardPlayer getNextPlayer();

    void startGame();

    void runGame();

    void endGame();

    boolean canPlayCard(CardPlayer player, Card card);

    @Nullable Card getBestCard(CardPlayer cards);

    boolean isGameOver();

    AbstractCardMenu<G> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck);

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
        getPlayers().forEach(p -> p.tick(this));
    }

    default int getMinPlayers() {
        return 2;
    }

    default int getMaxPlayers() {
        return 8;
    }

    static boolean canPlayGame(CardGame<?> cardGame, CardDeck cardDeck) {
        List<Card> necessaryCards = cardGame.getValidDeck();
        cardDeck.getCards().forEach(necessaryCards::remove);
        return necessaryCards.isEmpty();
    }

    static void dealCards(LinkedList<Card> drawPile, CardPlayer player, List<Card> censoredHand, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawPile.pollLast();
            card.flip();
            player.getHand().add(card);
            censoredHand.add(Card.BLANK);
        }
    }

}