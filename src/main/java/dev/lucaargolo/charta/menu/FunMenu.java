package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.sound.ModSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FunMenu extends AbstractCardMenu<FunGame> {

    private final FunGame game;

    private int canDoLast = 0;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 1 -> game.currentSuit != null ? game.currentSuit.ordinal() : -1;
                case 2 -> game.reversed ? 1 : 0;
                case 3 -> game.drawStack;
                case 4 -> game.rules;
                case 0 -> game.canDoLast() ? 1 : canDoLast;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 1 -> game.currentSuit = value >= 0 ? Suit.values()[value] : null;
                case 2 -> game.reversed = value > 0;
                case 3 -> game.drawStack = value;
                case 4 -> game.rules = value;
                case 0 -> canDoLast = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    protected FunMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), CardDeck.STREAM_CODEC.decode(buf), buf.readVarIntArray());
    }

    public FunMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players) {
        super(ModMenus.FUN.get(), containerId, inventory, access, deck, players);
        this.game = CardGames.getGameForMenu(CardGames.FUN, access, deck, players);

        this.addTopPreview(players);
        //Draw pile
        addCardSlot(new CardSlot<>(this.game, FunGame::getDrawPile, 19, 30) {
            @Override
            public boolean canInsertCard(CardPlayer player, List<Card> cards) {
                return false;
            }

            @Override
            public boolean canRemoveCard(CardPlayer player) {
                return player == this.game.getCurrentPlayer() && this.game.canDraw;
            }

            @Override
            public void onRemove(CardPlayer player, Card card) {
                card.flip();
                player.getPlay(this.game).complete(null);
            }
        });

        //Play pile
        addCardSlot(new CardSlot<>(this.game, FunGame::getPlayPile, 84, 30) {
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

        addCardSlot(new CardSlot<>(this.game, g -> (cardPlayer == g.getCurrentPlayer() && g.isChoosingWild) ? g.suits : cardPlayer.getHand(), 140/2f - CardSlot.getWidth(CardSlot.Type.INVENTORY)/2f, -5, CardSlot.Type.INVENTORY) {
            @Override
            public void onInsert(CardPlayer player, Card card) {
                player.playSound(ModSounds.CARD_PLAY.get());
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).add(Card.BLANK);
            }

            @Override
            public void onRemove(CardPlayer player, Card card) {
                player.playSound(ModSounds.CARD_DRAW.get());
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).removeLast();
                super.onRemove(player, card);
            }
        });

        addDataSlots(data);

    }

    public boolean canDoLast() {
        return canDoLast > 0;
    }

    public Suit getCurrentSuit() {
        return data.get(1) >= 0 ? Suit.values()[data.get(1)] : null;
    }

    public boolean isReversed() {
        return data.get(2) > 0;
    }

    public int getDrawStack() {
        return data.get(3);
    }

    public boolean isRule(int rule) {
        return (data.get(4) & (1 << rule)) != 0;
    }

    @Override
    public FunGame getGame() {
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
