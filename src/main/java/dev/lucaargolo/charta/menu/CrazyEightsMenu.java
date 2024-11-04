package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CrazyEightsMenu extends AbstractCardMenu<CrazyEightsGame> {

    @Nullable
    private final CrazyEightsGame game;
    private final CardPlayer cardPlayer;

    protected CrazyEightsMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL, null);
    }

    public CrazyEightsMenu(int containerId, Inventory inventory, ContainerLevelAccess access, @Nullable CardDeck deck) {
        super(ModMenus.CRAZY_EIGHTS.get(), containerId, inventory, access, deck);
        this.game = access.evaluate((level, pos) -> {
            Optional<CardTableBlockEntity> cardTable = level.getBlockEntity(pos, ModBlockEntityTypes.CARD_TABLE.get());
            if(cardTable.isPresent() && cardTable.get().getGame() instanceof CrazyEightsGame crazyEightsGame) {
                return crazyEightsGame;
            }else{
                return null;
            }
        }).orElse(null);
        if(this.player instanceof LivingEntityMixed mixed) {
            this.cardPlayer = mixed.charta_getCardPlayer();
        }else{
            this.cardPlayer = null;
        }
        if(this.game != null && this.cardPlayer != null) {
            //Players censored hand preview
            int players = this.game.getPlayers().size();
            float totalWidth = CardSlot.getWidth(CardSlot.Type.EXTENDED_SMALL);
            float playersWidth = (players * totalWidth) + ((players - 1f) * (totalWidth / 10f));
            for (int i = 0; i < players; i++) {
                CardPlayer p = this.game.getPlayers().get(i);
                addCardSlot(new CardSlot<>(this.game, g -> g.getCensoredHand(p), (176 / 2f - playersWidth / 2f) + (i * (totalWidth + totalWidth / 10f)), -18, CardSlot.Type.EXTENDED_SMALL) {
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
                    player.getPlay(this.game).complete(null);
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
                    player.getPlay(this.game).complete(card);
                }
            });
            addCardSlot(new CardSlot<>(this.game, g -> cardPlayer.getHand(), 13, 95, CardSlot.Type.EXTENDED) {
                @Override
                public void onInsert(CardPlayer player, Card card) {
                    game.getCensoredHand(player).add(Card.BLANK);
                    if (player == this.game.getCurrentPlayer() && this.game.drawsLeft == 0 && this.game.getBestCard(player) == null) {
                        player.getPlay(this.game).complete(null);
                    }
                }

                @Override
                public void onRemove(CardPlayer player, Card card) {
                    game.getCensoredHand(player).removeLast();
                    super.onRemove(player, card);
                }
            });
        }
    }

    @Override
    public @Nullable CrazyEightsGame getGame() {
        return this.game;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.game != null && this.cardPlayer != null;
    }

}
