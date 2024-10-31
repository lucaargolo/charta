package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.*;
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
        //TODO: Move game to Block Entity
        CardPlayer cardPlayer;
        if(player instanceof CardPlayerMixed mixed) {
            cardPlayer = mixed.charta_getCardPlayer();
            this.game = new CrazyEightsGame(List.of(cardPlayer, new AutoPlayer(), new AutoPlayer(), new AutoPlayer()));
            if(!player.level().isClientSide) {
                this.game.startGame();
                this.game.runGame();
            }
        }else{
            cardPlayer = new AutoPlayer();
            this.game = new CrazyEightsGame(List.of());
        }

        //Players censored hand preview
        int players = this.game.getPlayers().size();
        float totalWidth = CardSlot.getWidth(CardSlot.Type.EXTENDED_SMALL);
        float playersWidth = (players * totalWidth) + ((players-1f) * (totalWidth/10f));
        for(int i = 0; i < players; i++) {
            CardPlayer p = this.game.getPlayers().get(i);
            addCardSlot(new CardSlot<>(this.game, g -> g.getCensoredHand(p), (176/2f - playersWidth/2f) + (i*(totalWidth + totalWidth/10f)), -18, CardSlot.Type.EXTENDED_SMALL) {
                @Override
                public boolean canInsertCard(CardPlayer player, List<Card> cards) {
                    return false;
                }

                @Override
                public boolean canRemoveCard(CardPlayer player) {
                    return false;
                }
            });
        }
        //Draw pile
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getDrawPile, 19, 27) {
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
        //Play pile
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getPlayPile, 120, 27) {
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
        addCardSlot(new CardSlot<>(this.game, g -> cardPlayer.getHand(), 13, 95, CardSlot.Type.EXTENDED) {
            @Override
            public void onInsert(CardPlayer player, Card card) {
                game.getCensoredHand(player).add(Card.BLANK);
                if(player == this.game.getCurrentPlayer() && this.game.drawsLeft == 0 && this.game.getBestCard(player) == null) {
                    CompletableFuture<Card> play = player.getPlay(this.game);
                    player.setPlay(new CompletableFuture<>());
                    play.complete(null);
                }
            }

            @Override
            public void onRemove(CardPlayer player, Card card) {
                game.getCensoredHand(player).removeLast();
                super.onRemove(player, card);
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
