package dev.lucaargolo.charta.game.solitaire;

import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.game.Deck;
import dev.lucaargolo.charta.game.GameSlot;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SolitaireMenu extends AbstractCardMenu<SolitaireGame> {

    private int canRestore = 0;
    private final ContainerData data = new ContainerData() {

        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> game.moves;
                case 1 -> game.time;
                case 2 -> game.canRestore() ? 1 : canRestore;
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> game.moves = value;
                case 1 -> game.time = value;
                case 2 -> canRestore = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public SolitaireMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), Deck.STREAM_CODEC.decode(buf), buf.readVarIntArray(), buf.readByteArray());
    }

    public SolitaireMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Deck deck, int[] players, byte[] options) {
        super(ModMenus.SOLITAIRE.get(), containerId, inventory, access, deck, players, options);

        //Fix carried cards being lost forever when the screen was closed :)
        GameSlot playerHand = this.game.getPlayerHand(this.cardPlayer);
        if(!playerHand.isEmpty()) {
            this.getCarriedCards().addAll(playerHand);
            this.game.getPlayerHand(this.cardPlayer).clear();
            this.game.getCensoredHand(this.cardPlayer).clear();
        }

        //Stock Pile
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(0), 5f, 5f));
        //Waste Pile
        addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(1), 5 + (CardImage.WIDTH * 1.5f + 5), 5f));

        //Foundation Piles
        for(int i = 0; i < 4; i++) {
            int s = 2 + i;
            addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(s), 5 + (CardImage.WIDTH * 1.5f + 5)*(3 + i), 5f));
        }

        //Tableau Piles
        for(int i = 0; i < 7; i++) {
            int s = 6 + i;
            addCardSlot(new CardSlot<>(this.game, g -> g.getSlot(s), 5 + (CardImage.WIDTH * 1.5f + 5) * i, 5f + CardImage.HEIGHT * 1.5f + 5, CardSlot.Type.VERTICAL));
        }

        addDataSlots(data);

    }

    public int getMoves() {
        return data.get(0);
    }

    public int getTime() {
        return data.get(1);
    }

    public boolean canRestore() {
        return canRestore > 0;
    }

    @Override
    public CardGames.Factory<SolitaireGame> getGameFactory() {
        return CardGames.SOLITAIRE;
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
