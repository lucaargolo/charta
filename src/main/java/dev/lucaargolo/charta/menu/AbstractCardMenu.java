package dev.lucaargolo.charta.menu;

import com.google.common.base.Suppliers;
import dev.lucaargolo.charta.game.*;
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

    protected final G game;

    public final NonNullList<CardSlot<G>> cardSlots = NonNullList.create();
    private final NonNullList<GameSlot> lastCardSlots = NonNullList.create();
    private final NonNullList<GameSlot> remoteCardSlots = NonNullList.create();

    private GameSlot carriedCards = new GameSlot();
    private GameSlot remoteCarriedCards = new GameSlot();

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

    public AbstractCardMenu(MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players, byte[] options) {
        super(menuType, containerId);
        this.game = CardGames.getGameForMenu(this.getGameFactory(), access, deck, players, options);
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
            addCardSlot(new CardSlot<>(game, g -> g.getCensoredHand(cardPlayer, p), 26 + (140 / 2f - playersWidth / 2f) + (i * (totalWidth + totalWidth / 10f)), 7, CardSlot.Type.PREVIEW) {
                @Override
                public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                    return false;
                }

                @Override
                public boolean canRemoveCard(CardPlayer player, int index) {
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

    public abstract CardGames.Factory<G> getGameFactory();

    public G getGame() {
        return game;
    }

    public GameSlot getCarriedCards() {
        return this.carriedCards;
    }

    public GameSlot getRemoteCarriedCards() {
        return this.remoteCarriedCards;
    }

    public void setCarriedCards(GameSlot cards) {
        this.carriedCards = cards;
    }

    public void setCarriedCards(int stateId, GameSlot cards) {
        this.carriedCards = cards;
        this.stateId = stateId;
    }

    public void setCards(int slotId, int stateId, List<Card> cards) {
        this.cardSlots.get(slotId).setCards(cards);
        this.stateId = stateId;
    }

    public GameSlot getRemoteCards(int slotId) {
        return this.remoteCardSlots.get(slotId);
    }

    protected <C extends CardSlot<G>> C addCardSlot(C slot) {
        slot.index = this.cardSlots.size();
        this.cardSlots.add(slot);
        this.lastCardSlots.add(new GameSlot());
        this.remoteCardSlots.add(new GameSlot());
        return slot;
    }

    public CardSlot<G> getCardSlot(int slotId) {
        return this.cardSlots.get(slotId);
    }

    private void synchronizeCardSlotToRemote(int slotIndex, GameSlot cards, Supplier<GameSlot> supplier) {
        if (!this.suppressRemoteUpdates) {
            GameSlot remoteCards = this.remoteCardSlots.get(slotIndex);
            if (!remoteCards.equals(cards)) {
                GameSlot suppliedCards = supplier.get();
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
                this.remoteCarriedCards = GameSlot.copyOf(carriedCards);
                if (this.synchronizer instanceof CardContainerSynchronizer cardContainerSynchronizer) {
                    cardContainerSynchronizer.sendCarriedCardsChange(this, this.remoteCarriedCards);
                }
            }
        }
    }

    @Override
    public void broadcastChanges() {
        for (int i = 0; i < this.cardSlots.size(); i++) {
            GameSlot cards = this.cardSlots.get(i).getSlot();
            Supplier<GameSlot> cardsSupplier = Suppliers.memoize(() -> GameSlot.copyOf(cards));
            this.triggerCardSlotListeners(i, cards, cardsSupplier);
            this.synchronizeCardSlotToRemote(i, cards, cardsSupplier);
        }
        this.synchronizeCarriedCardsToRemote();

        super.broadcastChanges();
    }

    @Override
    public void broadcastFullState() {
        for (int i = 0; i < this.cardSlots.size(); i++) {
            GameSlot cards = this.cardSlots.get(i).getSlot();
            this.triggerCardSlotListeners(i, cards, () -> GameSlot.copyOf(cards));
        }
        super.broadcastFullState();
    }

    private void triggerCardSlotListeners(int slotIndex, GameSlot cards, Supplier<GameSlot> supplier) {
        GameSlot lastCards = this.lastCardSlots.get(slotIndex);
        if (!lastCards.equals(cards)) {
            GameSlot suppliedCards = supplier.get();
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
            this.remoteCardSlots.set(i, GameSlot.copyOf(this.cardSlots.get(i).getSlot()));
        }

        this.remoteCarriedCards = GameSlot.copyOf(this.carriedCards);
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
        GameSlot carriedCards = this.getCarriedCards();
        if (!carriedCards.isEmpty()) {
            for(Card carriedCard : carriedCards.getCards()) {
                this.cardPlayer.getHand().add(carriedCard);
                this.getGame().getCensoredHand(this.cardPlayer).add(Card.BLANK);
            }
            this.setCarriedCards(new GameSlot());
        }

    }
}
