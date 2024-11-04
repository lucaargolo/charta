package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrazyEightsMenu extends AbstractCardMenu<CrazyEightsGame> {

    private final CrazyEightsGame game;
    private final CardPlayer cardPlayer;

    private int currentPlayer = 0;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> game.getCurrentPlayer() == cardPlayer ? 1 : currentPlayer;
                case 1 -> game.getPlayers().indexOf(game.getCurrentPlayer());
                case 2 -> game.drawsLeft;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> currentPlayer = value;
                case 1 -> game.setCurrentPlayer(value);
                case 2 -> game.drawsLeft = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected CrazyEightsMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.NULL, CardDeck.STREAM_CODEC.decode(buf), buf.readInt());
    }

    public CrazyEightsMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int players) {
        super(ModMenus.CRAZY_EIGHTS.get(), containerId, inventory, access, deck);
        this.game = CardGames.getGameForMenu(CardGames.CRAZY_EIGHTS, access, deck, players);
        this.cardPlayer = ((LivingEntityMixed) this.player).charta_getCardPlayer();

        //Players censored hand preview
        float totalWidth = CardSlot.getWidth(CardSlot.Type.PREVIEW);
        float playersWidth = (players * totalWidth) + ((players - 1f) * (totalWidth / 10f));
        for (int i = 0; i < players; i++) {
            CardPlayer p = this.game.getPlayers().get(i);
            addCardSlot(new CardSlot<>(this.game, g -> g.getCensoredHand(p), (140 / 2f - playersWidth / 2f) + (i * (totalWidth + totalWidth / 10f)), 7, CardSlot.Type.PREVIEW) {
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
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getDrawPile, 21, 30) {
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
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getPlayPile, 82, 30) {
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

        addCardSlot(new CardSlot<>(this.game, g -> cardPlayer.getHand(), 140/2f - CardSlot.getWidth(CardSlot.Type.INVENTORY)/2f, -5, CardSlot.Type.INVENTORY) {
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

        addDataSlots(data);

    }

    public boolean isCurrentPlayer() {
        return data.get(0) == 1;
    }

    public int getCurrentPlayer() {
        return data.get(1);
    }

    public int getDrawsLeft() {
        return data.get(2);
    }

    @Override
    public CrazyEightsGame getGame() {
        return this.game;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.game != null && this.cardPlayer != null && !this.game.isGameOver();
    }

}
