package dev.lucaargolo.charta.game.solitaire;

import dev.lucaargolo.charta.game.*;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SolitaireMenu extends AbstractCardMenu<SolitaireGame> {

    @Nullable
    private Card lastStockCard = null;
    private int lastTableauDraw = -1;

    public SolitaireMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, ContainerLevelAccess.create(inventory.player.level(), buf.readBlockPos()), CardDeck.STREAM_CODEC.decode(buf), buf.readVarIntArray(), buf.readByteArray());
    }

    public SolitaireMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CardDeck deck, int[] players, byte[] options) {
        super(ModMenus.SOLITAIRE.get(), containerId, inventory, access, deck, players, options);

        //Stock Pile
        addCardSlot(new CardSlot<>(this.game, g -> this.game.getSlot(0), 5f, 5f) {
            @Override
            public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                return false;
            }

            @Override
            public void onRemove(CardPlayer player, List<Card> cards) {
                super.onRemove(player, cards);
                lastStockCard = cards.getLast();
                lastStockCard.flip();
            }

            @Override
            public boolean removeAll() {
                return false;
            }
        });
        //Waste Pile
        addCardSlot(new CardSlot<>(this.game, g -> this.game.getSlot(1), 5 + (CardImage.WIDTH * 1.5f + 5), 5f) {
            @Override
            public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                return cards.size() == 1 && cards.getLast() == lastStockCard;
            }

            @Override
            public void onInsert(CardPlayer player, List<Card> cards) {
                super.onInsert(player, cards);
                lastStockCard = null;
                player.play(null);
            }

            @Override
            public void onRemove(CardPlayer player, List<Card> cards) {
                super.onRemove(player, cards);
                lastStockCard = cards.getLast();
            }

            @Override
            public boolean removeAll() {
                return false;
            }
        });

        int i = 0;

        //Foundation Piles
        for(Suit suit : List.of(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS)) {
            GameSlot slot = this.game.getSlot(2 + i);
            addCardSlot(new CardSlot<>(this.game, g -> slot, 5 + (CardImage.WIDTH * 1.5f + 5)*(3 + i++), 5f) {
                @Override
                public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                    if(index != -1 && index != slot.size()) {
                        return false;
                    }else{
                        int i = slot.isEmpty() ? 0 : slot.getLast().getRank().ordinal();
                        for(Card card : cards) {
                            if(card.getSuit() != suit || card.getRank().ordinal() != 1 + i++) {
                                return false;
                            }
                        }
                        return true;
                    }
                }

                @Override
                public void onInsert(CardPlayer player, List<Card> cards) {
                    super.onInsert(player, cards);
                    lastStockCard = null;
                    if(lastTableauDraw >= 0) {
                        player.play(cards, lastTableauDraw);
                    }else{
                        player.play(null);
                    }
                    lastTableauDraw = -1;
                }

                @Override
                public boolean canRemoveCard(CardPlayer player, int index) {
                    return false;
                }
            });
        }

        //Tableau Piles
        for(i = 0; i < 7; i++) {
            int s = 6 + i;
            GameSlot slot = this.game.getSlot(s);
            addCardSlot(new CardSlot<>(this.game, g -> slot, 5 + (CardImage.WIDTH * 1.5f + 5) * i, 5f + CardImage.HEIGHT * 1.5f + 5, CardSlot.Type.VERTICAL) {
                @Override
                public boolean canRemoveCard(CardPlayer player, int index) {
                    if(index == -1) {
                        return slot.size() == 1;
                    }
                    Card last = null;
                    for(int i = index; i < slot.size(); i++) {
                        Card current = slot.get(i);
                        if(last != null && (last.isFlipped() || !SolitaireGame.isAlternate(last, current))) {
                            return false;
                        }
                        last = current;
                    }
                    return true;
                }

                @Override
                public void onRemove(CardPlayer player, List<Card> cards) {
                    super.onRemove(player, cards);
                    lastTableauDraw = s;
                }

                @Override
                public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                    if(lastTableauDraw == s) {
                        return true;
                    }
                    Card last = slot.isEmpty() ? null : slot.getLast();
                    for(Card current : cards) {
                        if((last == null && current.getRank() != Rank.KING) || ((last != null && !SolitaireGame.isAlternate(last, current)) || (last != null && current.getRank().ordinal()+1 != last.getRank().ordinal()))) {
                            return false;
                        }
                        last = current;
                    }
                    return true;
                }

                @Override
                public void onInsert(CardPlayer player, List<Card> cards) {
                    super.onInsert(player, cards);
                    lastStockCard = null;
                    if(lastTableauDraw != s && lastTableauDraw >= 0) {
                        player.play(cards, lastTableauDraw);
                    }else{
                        player.play(null);
                    }
                    lastTableauDraw = -1;
                }
            });
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
