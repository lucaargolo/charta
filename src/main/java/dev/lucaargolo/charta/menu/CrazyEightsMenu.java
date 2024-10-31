package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.AutoPlayer;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.CrazyEightsGame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CrazyEightsMenu extends AbstractCardMenu<CrazyEightsGame> {

    private final CrazyEightsGame game;

    protected CrazyEightsMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public CrazyEightsMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.CRAZY_EIGHTS.get(), containerId, inventory, access);
        CardPlayer cardPlayer;
        if(player instanceof CardPlayer) {
            cardPlayer = (CardPlayer) player;
            this.game = new CrazyEightsGame(List.of(cardPlayer, new AutoPlayer(), new AutoPlayer(), new AutoPlayer()));
            this.game.startGame();
            this.game.runGame();
        }else{
            cardPlayer = new AutoPlayer();
            this.game = new CrazyEightsGame(List.of());
        }

        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getDrawPile, 0, 0) {
            @Override
            public boolean canInsertCard(CardPlayer player, List<Card> cards) {
                return false;
            }

            @Override
            public boolean canRemoveCard(CardPlayer player) {
                return player == this.game.getCurrentPlayer() && this.game.drawsLeft > 0;
            }

            @Override
            public void onRemove(CardPlayer player, Card card) {
                card.flip();
                CompletableFuture<Card> play = player.getPlay(this.game);
                player.setPlay(new CompletableFuture<>());
                play.complete(null);
            }
        });
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getPlayPile, 80, 0) {
            @Override
            public boolean canInsertCard(CardPlayer player, List<Card> cards) {
                return player == this.game.getCurrentPlayer() && cards.size() == 1 && this.game.canPlayCard(player, cards.getLast());
            }

            @Override
            public boolean canRemoveCard(CardPlayer player) {
                return false;
            }

            @Override
            public void onInsert(CardPlayer player, Card card) {
                CompletableFuture<Card> play = player.getPlay(this.game);
                player.setPlay(new CompletableFuture<>());
                play.complete(card);
            }
        });
        addCardSlot(new CardSlot<>(this.game, game -> cardPlayer.getHand(), 0, 80, true) {
            @Override
            public void onInsert(CardPlayer player, Card card) {
                if(player == this.game.getCurrentPlayer() && this.game.drawsLeft == 0 && this.game.getBestCard(player) == null) {
                    CompletableFuture<Card> play = player.getPlay(this.game);
                    player.setPlay(new CompletableFuture<>());
                    play.complete(null);
                }
            }
        });
    }

    @Override
    public CrazyEightsGame getGame() {
        return game;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

}
