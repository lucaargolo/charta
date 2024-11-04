package dev.lucaargolo.charta.menu;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.utils.CardContainerListener;
import dev.lucaargolo.charta.utils.CardContainerSynchronizer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractCardMenu<G extends CardGame> extends AbstractContainerMenu {

    public final NonNullList<CardSlot<G>> cardSlots = NonNullList.create();
    private final NonNullList<ImmutableList<Card>> lastCardSlots = NonNullList.create();
    private final NonNullList<ImmutableList<Card>> remoteCardSlots = NonNullList.create();

    private ImmutableList<Card> carriedCards = ImmutableList.of();
    private ImmutableList<Card> remoteCarriedCards = ImmutableList.of();;

    @Nullable
    protected final CardDeck deck;
    protected final Inventory inventory;
    protected final Player player;
    protected final ContainerLevelAccess access;

    public AbstractCardMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, @Nullable CardDeck deck) {
        super(menuType, containerId);
        this.inventory = inventory;
        this.player = inventory.player;
        this.access = access;
        this.deck = deck;
    }

    public abstract @Nullable G getGame();

    public ImmutableList<Card> getCarriedCards() {
        return this.carriedCards;
    }

    public ImmutableList<Card> getRemoteCarriedCards() {
        return this.remoteCarriedCards;
    }


    public void setCarriedCards(ImmutableList<Card> cards) {
        this.carriedCards = cards;
    }

    public void setCarriedCards(int stateId, ImmutableList<Card> cards) {
        this.carriedCards = cards;
        this.stateId = stateId;
    }

    public void setCards(int slotId, int stateId, List<Card> cards) {
        this.cardSlots.get(slotId).setCards(cards);
        this.stateId = stateId;
    }

    public List<Card> getRemoteCards(int slotId) {
        return this.remoteCardSlots.get(slotId);
    }

    protected CardSlot<G> addCardSlot(CardSlot<G> slot) {
        slot.index = this.cardSlots.size();
        this.cardSlots.add(slot);
        this.lastCardSlots.add(ImmutableList.of());
        this.remoteCardSlots.add(ImmutableList.of());
        return slot;
    }

    public CardSlot<G> getCardSlot(int slotId) {
        return this.cardSlots.get(slotId);
    }

    private void synchronizeCardSlotToRemote(int slotIndex, List<Card> cards, Supplier<ImmutableList<Card>> supplier) {
        if (!this.suppressRemoteUpdates) {
            ImmutableList<Card> remoteCards = this.remoteCardSlots.get(slotIndex);
            if (!remoteCards.equals(cards)) {
                ImmutableList<Card> suppliedCards = supplier.get();
                this.remoteCardSlots.set(slotIndex, suppliedCards);
                if (this.synchronizer instanceof CardContainerSynchronizer cardContainerSynchronizer) {
                    cardContainerSynchronizer.sendCardSlotChange(this, slotIndex, suppliedCards);
                }
            }
        }
    }

    private void synchronizeCarriedCardsToRemote() {
        if (!this.suppressRemoteUpdates) {
            if (!remoteCarriedCards.equals(carriedCards)) {
                this.remoteCarriedCards = ImmutableList.copyOf(carriedCards);
                if (this.synchronizer instanceof CardContainerSynchronizer cardContainerSynchronizer) {
                    cardContainerSynchronizer.sendCarriedCardsChange(this, this.remoteCarriedCards);
                }
            }
        }
    }

    @Override
    public void broadcastChanges() {
        for (int i = 0; i < this.cardSlots.size(); i++) {
            List<Card> cards = this.cardSlots.get(i).getCards();
            Supplier<ImmutableList<Card>> cardsSupplier = Suppliers.memoize(() -> ImmutableList.copyOf(cards));
            this.triggerCardSlotListeners(i, cards, cardsSupplier);
            this.synchronizeCardSlotToRemote(i, cards, cardsSupplier);
        }
        this.synchronizeCarriedCardsToRemote();

        super.broadcastChanges();
    }

    @Override
    public void broadcastFullState() {
        for (int i = 0; i < this.cardSlots.size(); i++) {
            List<Card> cards = this.cardSlots.get(i).getCards();
            this.triggerCardSlotListeners(i, cards, () -> ImmutableList.copyOf(cards));
        }
        super.broadcastFullState();
    }

    private void triggerCardSlotListeners(int slotIndex, List<Card> cards, Supplier<ImmutableList<Card>> supplier) {
        List<Card> lastCards = this.lastCardSlots.get(slotIndex);
        if (!lastCards.equals(cards)) {
            ImmutableList<Card> suppliedCards = supplier.get();
            this.lastCardSlots.set(slotIndex, suppliedCards);

            for (ContainerListener containerlistener : this.containerListeners) {
                if(containerlistener instanceof CardContainerListener cardContainerListener) {
                    cardContainerListener.cardChanged(this, slotIndex, suppliedCards);
                }
            }
        }
    }

    @Override
    public void sendAllDataToRemote() {
        int i = 0;
        for (int j = this.cardSlots.size(); i < j; i++) {
            this.remoteCardSlots.set(i, ImmutableList.copyOf(this.cardSlots.get(i).getCards()));
        }

        this.remoteCarriedCards = ImmutableList.copyOf(this.carriedCards);
        super.sendAllDataToRemote();
    }

    @Override
    public void transferState(@NotNull AbstractContainerMenu menu) {
        super.transferState(menu);
        if(menu instanceof AbstractCardMenu<?> cardMenu) {
            for (int j = 0; j < this.cardSlots.size(); j++) {
                this.lastCardSlots.set(j, cardMenu.lastCardSlots.get(j));
                this.remoteCardSlots.set(j, cardMenu.remoteCardSlots.get(j));
            }
        }
    }

}
