package dev.lucaargolo.charta.game.solitaire;

import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.menu.CardSlot;
import dev.lucaargolo.charta.menu.ModMenus;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SolitaireMenu extends AbstractCardMenu<SolitaireGame> {

    public SolitaireMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), CardDeck.STREAM_CODEC.decode(buf), buf.readVarIntArray(), buf.readByteArray());
    }

    public SolitaireMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players, byte[] options) {
        super(ModMenus.SOLITAIRE, containerId, inventory, access, deck, players, options);

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
