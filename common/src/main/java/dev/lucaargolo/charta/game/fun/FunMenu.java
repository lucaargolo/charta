package dev.lucaargolo.charta.game.fun;

import dev.lucaargolo.charta.game.GameType;
import dev.lucaargolo.charta.game.ModGameTypes;
import dev.lucaargolo.charta.game.Suit;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.HandSlot;
import dev.lucaargolo.charta.menu.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FunMenu extends AbstractCardMenu<FunGame, FunMenu> {

    private int canDoLast = 0;
    private int didntSayLast = 0;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> game.canDoLast() ? 1 : canDoLast;
                case 1 -> game.didntSayLast(cardPlayer) ? 1 : didntSayLast;
                case 2 -> game.currentSuit != null ? game.currentSuit.id() : -1;
                case 3 -> game.reversed ? 1 : 0;
                case 4 -> game.drawStack;
                case 5 -> game.canDraw ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> canDoLast = value;
                case 1 -> didntSayLast = value;
                case 2 -> game.currentSuit = value >= 0 ? Suit.getSuit(value) : null;
                case 3 -> game.reversed = value > 0;
                case 4 -> game.drawStack = value;
                case 5 -> game.canDraw = value > 0;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public FunMenu(int containerId, Inventory inventory, Definition definition) {
        super(ModMenuTypes.FUN.get(), containerId, inventory, definition);

        this.addTopPreview(definition.players());
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(0), 16, 30));
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(1), 87, 30));
        addCardSlot(new HandSlot<>(this.game, g -> !g.isChoosingWild, this.getCardPlayer(), 140/2f - CardSlot.getWidth(CardSlot.Type.HORIZONTAL)/2f, -5, CardSlot.Type.HORIZONTAL));
        addDataSlots(data);
    }

    public boolean canDoLast() {
        return canDoLast > 0;
    }

    public boolean didntSayLast() {
        return didntSayLast > 0;
    }

    public Suit getCurrentSuit() {
        return data.get(2) >= 0 ? Suit.getSuit(data.get(2)) : null;
    }

    public boolean isReversed() {
        return data.get(3) > 0;
    }

    public int getDrawStack() {
        return data.get(4);
    }

    public boolean canDraw() {
        return data.get(5) > 0;
    }

    @Override
    public GameType<FunGame, FunMenu> getGameType() {
        return ModGameTypes.FUN.get();
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
