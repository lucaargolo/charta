package dev.lucaargolo.charta.common.game.impl.crazyeights;

import dev.lucaargolo.charta.common.game.Games;
import dev.lucaargolo.charta.common.game.Suits;
import dev.lucaargolo.charta.common.game.api.card.Suit;
import dev.lucaargolo.charta.common.game.api.game.GameType;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.common.menu.CardSlot;
import dev.lucaargolo.charta.common.menu.HandSlot;
import dev.lucaargolo.charta.common.menu.ModMenuTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrazyEightsMenu extends AbstractCardMenu<CrazyEightsGame, CrazyEightsMenu> {

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> game.drawsLeft;
                case 1 -> game.currentSuit != null ? Suits.getRegistry().getId(game.currentSuit) : -1;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> game.drawsLeft = (byte) value;
                case 1 -> game.currentSuit = value >= 0 ? Suits.getRegistry().getHolder(value).map(Holder::value).orElse(null) : null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };


    public CrazyEightsMenu(int containerId, Inventory inventory, Definition definition) {
        super(ModMenuTypes.CRAZY_EIGHTS.get(), containerId, inventory, definition);

        this.addTopPreview(definition.players());
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(0), 16, 30));
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(1), 87, 30));
        addCardSlot(new HandSlot<>(this.game, g -> !g.isChoosingWild, this.getCardPlayer(), 140/2f - CardSlot.getWidth(CardSlot.Type.HORIZONTAL)/2f, -5, CardSlot.Type.HORIZONTAL));
        addDataSlots(data);
    }

    public int getDrawsLeft() {
        return data.get(0);
    }

    public Suit getCurrentSuit() {
        return data.get(1) >= 0 ? Suits.getRegistry().getHolder(data.get(1)).map(Holder::value).orElse(null) : null;
    }

    @Override
    public GameType<CrazyEightsGame, CrazyEightsMenu> getGameType() {
        return Games.CRAZY_EIGHTS.get();
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
