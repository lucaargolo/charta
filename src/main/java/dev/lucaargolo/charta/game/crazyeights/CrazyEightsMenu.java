package dev.lucaargolo.charta.game.crazyeights;

import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.menu.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrazyEightsMenu extends AbstractCardMenu<CrazyEightsGame> {

    private final DrawSlot<CrazyEightsGame> drawSlot;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> game.drawsLeft;
                case 1 -> game.currentSuit != null ? game.currentSuit.ordinal() : -1;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> game.drawsLeft = (byte) value;
                case 1 -> game.currentSuit = value >= 0 ? Suit.values()[value] : null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CrazyEightsMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), CardDeck.STREAM_CODEC.decode(buf), buf.readVarIntArray(), buf.readByteArray());
    }

    public CrazyEightsMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players, byte[] options) {
        super(ModMenus.CRAZY_EIGHTS.get(), containerId, inventory, access, deck, players, options);

        this.addTopPreview(players);
        //Draw pile
        this.drawSlot = addCardSlot(new DrawSlot<>(this.game, g -> this.game.getSlot(0), 16, 30, () -> this.game.drawsLeft > 0));
        //Play pile
        addCardSlot(new PlaySlot<>(this.game, g -> this.game.getSlot(1), 87, 30, drawSlot));

        addCardSlot(new CardSlot<>(this.game, g -> (cardPlayer == g.getCurrentPlayer() && g.isChoosingWild) ? g.suits : cardPlayer.getHand(), 140/2f - CardSlot.getWidth(CardSlot.Type.HORIZONTAL)/2f, -5, CardSlot.Type.HORIZONTAL) {
            @Override
            public void onInsert(CardPlayer player, List<Card> cards) {
                super.onInsert(player, cards);
                if(drawSlot.isDraw()) {
                    player.play(null);
                    drawSlot.setDraw(false);
                }
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).add(Card.BLANK);
            }

            @Override
            public void onRemove(CardPlayer player, List<Card> cards) {
                super.onRemove(player, cards);
                if(!game.isChoosingWild)
                    game.getCensoredHand(player).removeLast();
            }

            @Override
            public boolean removeAll() {
                return false;
            }
        });

        addDataSlots(data);

    }

    public int getDrawsLeft() {
        return data.get(0);
    }

    public Suit getCurrentSuit() {
        return data.get(1) >= 0 ? Suit.values()[data.get(1)] : null;
    }

    @Override
    public CardGames.Factory<CrazyEightsGame> getGameFactory() {
        return CardGames.CRAZY_EIGHTS;
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
