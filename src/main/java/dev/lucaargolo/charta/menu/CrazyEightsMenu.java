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

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> game.drawsLeft;
                case 1 -> game.currentSuit.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> game.drawsLeft = value;
                case 1 -> game.currentSuit = Card.Suit.values()[value];
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    protected CrazyEightsMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), CardDeck.STREAM_CODEC.decode(buf), buf.readVarIntArray());
    }

    public CrazyEightsMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players) {
        super(ModMenus.CRAZY_EIGHTS.get(), containerId, inventory, access, deck, players);
        this.game = CardGames.getGameForMenu(CardGames.CRAZY_EIGHTS, access, deck, players);

        this.addTopPreview(players);
        //Draw pile
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getDrawPile, 19, 30) {
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
        addCardSlot(new CardSlot<>(this.game, CrazyEightsGame::getPlayPile, 84, 30) {
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

        addCardSlot(new CardSlot<>(this.game, g -> g.isChoosingWild ? g.suits : cardPlayer.getHand(), 140/2f - CardSlot.getWidth(CardSlot.Type.INVENTORY)/2f, -5, CardSlot.Type.INVENTORY) {
            @Override
            public void onInsert(CardPlayer player, Card card) {
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).add(Card.BLANK);
                if (player == this.game.getCurrentPlayer() && this.game.drawsLeft == 0 && this.game.getBestCard(player) == null) {
                    player.getPlay(this.game).complete(null);
                }
            }

            @Override
            public void onRemove(CardPlayer player, Card card) {
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).removeLast();
                super.onRemove(player, card);
            }
        });

        addDataSlots(data);

    }

    public int getDrawsLeft() {
        return data.get(0);
    }

    public Card.Suit getCurrentSuit() {
        return Card.Suit.values()[data.get(1)];
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
