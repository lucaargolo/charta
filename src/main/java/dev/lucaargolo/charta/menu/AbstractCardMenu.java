package dev.lucaargolo.charta.menu;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.CardContainerListener;
import dev.lucaargolo.charta.utils.CardContainerSynchronizer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractCardMenu<G extends CardGame<G>> extends AbstractContainerMenu {

    public final NonNullList<CardSlot<G>> cardSlots = NonNullList.create();
    private final NonNullList<ImmutableList<Card>> lastCardSlots = NonNullList.create();
    private final NonNullList<ImmutableList<Card>> remoteCardSlots = NonNullList.create();

    private ImmutableList<Card> carriedCards = ImmutableList.of();
    private ImmutableList<Card> remoteCarriedCards = ImmutableList.of();;

    protected final Inventory inventory;
    protected final ContainerLevelAccess access;
    protected final CardDeck deck;
    protected final Player player;
    protected final CardPlayer cardPlayer;

    private int currentPlayer = 0;
    private int gameReady = 0;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            G game = getGame();
            return switch (index) {
                case 0 -> game.getCurrentPlayer() == cardPlayer ? 1 : currentPlayer;
                case 1 -> game.getPlayers().indexOf(game.getCurrentPlayer());
                case 2 -> game.isGameReady() ? 1 : gameReady;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            G game = getGame();
            switch (index) {
                case 0 -> currentPlayer = value;
                case 1 -> game.setCurrentPlayer(value);
                case 2 -> gameReady = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public AbstractCardMenu(MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players) {
        super(menuType, containerId);
        this.inventory = inventory;
        this.player = inventory.player;
        this.access = access;
        this.deck = deck;
        this.cardPlayer = ((LivingEntityMixed) this.player).charta_getCardPlayer();
        this.addDataSlots(data);
    }

    public void addTopPreview(int[] players) {
        float totalWidth = CardSlot.getWidth(CardSlot.Type.PREVIEW) + 28;
        float playersWidth = (players.length * totalWidth) + ((players.length - 1f) * (totalWidth / 10f));
        for (int i = 0; i < players.length; i++) {
            G game = this.getGame();
            CardPlayer p = game.getPlayers().get(i);
            addCardSlot(new CardSlot<>(game, g -> g.getCensoredHand(p), 26 + (140 / 2f - playersWidth / 2f) + (i * (totalWidth + totalWidth / 10f)), 7, CardSlot.Type.PREVIEW) {
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
    }

    public CardPlayer getCardPlayer() {
        return cardPlayer;
    }

    public boolean isCurrentPlayer() {
        return data.get(0) == 1;
    }

    public CardPlayer getCurrentPlayer() {
        return this.getGame().getPlayers().get(data.get(1));
    }

    public boolean isGameReady() {
        return data.get(2) == 1;
    }

    public CardDeck getDeck() {
        return deck;
    }

    public abstract G getGame();

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

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        List<Card> carriedCards = this.getCarriedCards();
        if (!carriedCards.isEmpty()) {
            for(Card carriedCard : carriedCards) {
                this.cardPlayer.getHand().add(carriedCard);
                this.getGame().getCensoredHand(this.cardPlayer).add(Card.BLANK);
            }
            this.setCarriedCards(ImmutableList.of());
        }

    }
}
