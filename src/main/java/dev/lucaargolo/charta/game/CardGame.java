package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.menu.AbstractCardMenu;
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
import java.util.LinkedList;
import java.util.List;

public interface CardGame<G extends CardGame<G>> {

    static CardPlayer TABLE = new AutoPlayer(0f) {
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

    boolean isGameOver();

    AbstractCardMenu<G> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck);

    default TransparentLinkedList<Card> getCensoredHand(CardPlayer player) {
        return getCensoredHand(null, player);
    }

    default @Nullable Card getBestCard(CardPlayer player) {
        return player.getHand().stream().filter(c -> canPlayCard(player, c)).findFirst().orElse(null);
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
        getPlayers().forEach(p -> p.tick(this));
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