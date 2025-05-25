package dev.lucaargolo.charta.game.solitaire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.CardTableBlockEntity;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.sound.ModSounds;
import dev.lucaargolo.charta.utils.CardImage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SolitaireGame extends CardGame<SolitaireGame> {

    private final List<Snapshot> snapshots = new ArrayList<>();

    private final GameSlot stockPile;
    private final GameSlot wastePile;

    private final Map<Suit, GameSlot> foundationPiles;
    private final List<GameSlot> tableauPiles;

    @Nullable
    private Card lastStockCard = null;
    private int lastTableauDraw = -1;

    private int age = 0;
    private boolean taken = false;

    public int moves = 0;
    public int time = 0;

    public SolitaireGame(List<CardPlayer> players, CardDeck deck) {
        super(players, deck);

        float middleX = CardTableBlockEntity.TABLE_WIDTH/2f;
        float leftX = middleX - CardImage.WIDTH/2f - (CardImage.WIDTH + 5)*3;

        float middleY = CardTableBlockEntity.TABLE_HEIGHT/2f;
        float topY = middleY + (1.75f * CardImage.HEIGHT);

        this.stockPile = addSlot(new GameSlot(new LinkedList<>(), leftX, topY, 0f, 0f) {
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
        this.stockPile.highlightColor = 0xf192fc;

        this.wastePile = addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + 5, topY, 0f, 0f) {
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
        this.wastePile.highlightColor = 0xfc605d;

        int i = 0;
        ImmutableMap.Builder<Suit, GameSlot> map = ImmutableMap.builder();
        for(Suit suit : List.of(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS)) {
            GameSlot slot = addSlot(new GameSlot(new LinkedList<>(), leftX + (CardImage.WIDTH + 5)*(3+i++), topY, 0f, 0f) {
                @Override
                public boolean canRemoveCard(CardPlayer player, int index) {
                    return index == size()-1 || index == -1;
                }

                @Override
                public boolean canInsertCard(CardPlayer player, List<Card> cards, int index) {
                    if(index != -1 && index != this.size()) {
                        return false;
                    }else{
                        int i = this.isEmpty() ? 0 : this.getLast().rank().ordinal();
                        for(Card card : cards) {
                            if(card.suit() != suit || card.rank().ordinal() != 1 + i++) {
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
                public boolean removeAll() {
                    return false;
                }
            });
            slot.highlightColor = 0x93ff9c;
            map.put(suit, slot);
        }
        this.foundationPiles = map.build();

        ImmutableList.Builder<GameSlot> list = ImmutableList.builder();
        for(i = 0; i < 7; i++) {
            int s = 6 + i;
            GameSlot slot = addSlot(new GameSlot(new LinkedList<>(), leftX + CardImage.WIDTH + (CardImage.WIDTH + 5)*i, topY + 5f + CardImage.HEIGHT - (CardImage.HEIGHT*1.5f), 0f, 180f, Direction.NORTH, CardImage.HEIGHT*3, false)  {
                @Override
                public boolean canRemoveCard(CardPlayer player, int index) {
                    if(index == -1) {
                        return this.size() == 1;
                    }
                    Card last = null;
                    for(int i = index; i < this.size(); i++) {
                        Card current = this.get(i);
                        if(last != null && (last.flipped() || !SolitaireGame.isAlternate(last, current))) {
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
                    if(index != -1 && index != this.size()-1) {
                        return false;
                    }
                    if(lastTableauDraw == s) {
                        return true;
                    }
                    Card last = this.isEmpty() ? null : this.getLast();
                    for(Card current : cards) {
                        if((last == null && current.rank() != Rank.KING) || ((last != null && !SolitaireGame.isAlternate(last, current)) || (last != null && current.rank().ordinal()+1 != last.rank().ordinal()))) {
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
            slot.highlightColor = 0xfff570;
            list.add(slot);
        }
        this.tableauPiles = list.build();
    }

    @Override
    public AbstractCardMenu<SolitaireGame> createMenu(int containerId, Inventory playerInventory, ServerLevel level, BlockPos pos, CardDeck deck) {
        return new SolitaireMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), deck, players.stream().mapToInt(CardPlayer::getId).toArray(), this.getRawOptions());
    }

    @Override
    public Predicate<CardDeck> getDeckPredicate() {
        return (deck) -> {
            return deck.getCards().size() == 52 && Charta.DEFAULT_SUITS.containsAll(deck.getUniqueSuits()) && deck.getUniqueSuits().containsAll(Charta.DEFAULT_SUITS);
        };
    }

    @Override
    public Predicate<Card> getCardPredicate() {
        return (card) -> Charta.DEFAULT_SUITS.contains(card.suit()) && Charta.DEFAULT_RANKS.contains(card.rank());
    }


    @Override
    public boolean canPlay(CardPlayer player, CardPlay play) {
        return false;
    }

    @Override
    public void startGame() {
        this.stockPile.clear();
        this.wastePile.clear();

        this.foundationPiles.values().forEach(GameSlot::clear);
        this.tableauPiles.forEach(GameSlot::clear);

        this.stockPile.addAll(gameDeck);
        this.stockPile.shuffle();

        for (CardPlayer player : players) {
            player.resetPlay();
            this.getPlayerHand(player).clear();
            this.getCensoredHand(player).clear();
        }

        for (int i = 0; i < this.tableauPiles.size(); i++) {
            for (int j = 0; j < this.tableauPiles.size(); j++) {
                int slot = j;
                int amount = i;
                if(slot >= amount) {
                    this.scheduledActions.add(() -> {
                        this.currentPlayer.playSound(ModSounds.CARD_DRAW.get());
                        Card card = this.stockPile.removeLast();
                        if(slot == amount) card.flip();
                        this.tableauPiles.get(slot).addLast(card);
                    });
                    this.scheduledActions.add(() -> {});
                }
            }
        }

        this.scheduledActions.add(() -> Snapshot.create(this));

        this.currentPlayer = players.getFirst();
        this.isGameReady = false;
        this.isGameOver = false;

        table(Component.translatable("message.charta.game_started"));
    }

    @Override
    public void tick() {
        super.tick();
        if(isGameReady) {
            this.age++;
            if (age % 20 == 0) {
                this.time++;
            }
        }
    }

    @Override
    public void runGame() {
        if(!isGameReady) {
            return;
        }
        currentPlayer.afterPlay(play -> {
            //Setup next play.
            currentPlayer.resetPlay();

            if(play != null) {
                //If they successfully did a play, unflip the last card from the tableau
                GameSlot s = this.getSlot(play.slot());
                if(!s.isEmpty() && s.getLast().flipped()) {
                    s.getLast().flip();
                    s.setDirty(true);
                    play(currentPlayer, Component.translatable("message.charta.revealed_a_card", Component.translatable(deck.getCardTranslatableKey(s.getLast())), play.slot()-5));
                }else{
                    play(currentPlayer, Component.translatable("message.charta.did_a_move"));
                }
            }else{
                play(currentPlayer, Component.translatable("message.charta.did_a_move"));
            }

            //Check if the stockpile is empty
            if(this.stockPile.isEmpty()) {
                this.wastePile.forEach(Card::flip);
                this.wastePile.reverse();
                this.stockPile.addAll(this.wastePile);
                this.wastePile.clear();
            }

            boolean allEmpty = true;
            for(GameSlot slot : this.tableauPiles) {
                allEmpty = allEmpty && slot.isEmpty();
            }

            if(allEmpty) {
                endGame();
            }else{
                runGame();
            }
        });
    }

    @Override
    public void endGame() {
        boolean allEmpty = true;
        for(GameSlot slot : this.tableauPiles) {
            allEmpty = allEmpty && slot.isEmpty();
        }
        if(allEmpty) {
            currentPlayer.sendTitle(Component.translatable("message.charta.you_won").withStyle(ChatFormatting.GREEN), Component.translatable("message.charta.congratulations"));
        }else{
            currentPlayer.sendTitle(Component.translatable("message.charta.you_lost").withStyle(ChatFormatting.RED), Component.translatable("message.charta.give_up"));
        }

        this.isGameOver = true;

    }

    public boolean canRestore() {
        return this.moves > 0 && !this.snapshots.isEmpty();
    }

    public void restore() {
        if(!this.snapshots.isEmpty()) {
            this.currentPlayer.playSound(ModSounds.CARD_DRAW.get());
            Snapshot snapshot = this.snapshots.removeLast();
            snapshot.restore(this);
            this.moves++;
            this.currentPlayer.playSound(ModSounds.CARD_PLAY.get());
        }
    }

    @Override
    public void preUpdate() {
        if(!taken) {
            Snapshot current = Snapshot.create(this);
            if(!snapshots.isEmpty()) {
                Snapshot last = snapshots.getLast();
                if (!current.equals(last)) {
                    snapshots.addLast(current);
                }
            }else{
                snapshots.addLast(current);
            }
        }
    }

    @Override
    public void postUpdate() {
        if(taken) {
            if(!snapshots.isEmpty()) {
                Snapshot current = Snapshot.create(this);
                Snapshot last = snapshots.getLast();
                if(!current.equals(last)) {
                    moves++;
                }
            }else{
                moves++;
            }
            taken = false;
        }else{
            taken = true;
        }
    }

    @Override
    public int getMinPlayers() {
        return 1;
    }

    @Override
    public int getMaxPlayers() {
        return 1;
    }

    @Override
    public List<GameOption<?>> getOptions() {
        return List.of();
    }

    public static boolean isAlternate(Card c1, Card c2) {
        boolean v1 = (c1.suit() == Suit.HEARTS || c1.suit() == Suit.DIAMONDS);
        boolean v2 = (c2.suit() == Suit.HEARTS || c2.suit() == Suit.DIAMONDS);
        return v1 != v2;
    }

    public Pair<Component, List<GameSlot>> getHint() {
        // Check if any card from tableau can be moved to foundation
        for (GameSlot slot : tableauPiles) {
            if (!slot.isEmpty()) {
                Card card = slot.getLast();
                if (foundationPiles.get(card.suit()).canInsertCard(currentPlayer, List.of(card), -1)) {
                    return Pair.of(
                        Component.translatable("message.charta.move_card_from_tableau_to_foundation", Component.translatable(deck.getCardTranslatableKey(card)).withColor(deck.getCardColor(card))),
                        List.of(slot, foundationPiles.get(card.suit()))
                    );
                }
            }
        }

        // Check if any card from waste can be moved to foundation
        if (!wastePile.isEmpty()) {
            Card card = wastePile.getLast();
            if (foundationPiles.get(card.suit()).canInsertCard(currentPlayer, List.of(card), -1)) {
                return Pair.of(
                    Component.translatable("message.charta.move_card_from_waste_to_foundation", Component.translatable(deck.getCardTranslatableKey(card)).withColor(deck.getCardColor(card))),
                    List.of(wastePile, foundationPiles.get(card.suit()))
                );
            }
        }

        // Check for moves between tableau columns
        for (GameSlot fromSlot : tableauPiles) {
            for (int i = 0; i < fromSlot.size(); i++) {
                Card card = fromSlot.get(i);
                if (card.flipped()) continue;

                for (GameSlot toSlot : tableauPiles) {
                    if (fromSlot == toSlot) continue;
                    if (toSlot.canInsertCard(currentPlayer, List.of(card), -1)) {
                        return Pair.of(
                            Component.translatable("message.charta.move_card_from_tableau_to_tableau", Component.translatable(deck.getCardTranslatableKey(card)).withColor(deck.getCardColor(card))),
                            List.of(fromSlot, toSlot)
                        );
                    }
                }
            }
        }

        // Check if waste can be moved to tableau
        if (!wastePile.isEmpty()) {
            Card card = wastePile.getLast();
            for (GameSlot slot : tableauPiles) {
                if (slot.canInsertCard(currentPlayer, List.of(card), -1)) {
                    return Pair.of(
                        Component.translatable("message.charta.move_card_from_waste_to_tableau", Component.translatable(deck.getCardTranslatableKey(card)).withColor(deck.getCardColor(card))),
                        List.of(wastePile, slot)
                    );
                }
            }
        }

        return Pair.of(
            Component.translatable("message.charta.no_moves_available"),
            List.of(stockPile)
        );
    }

    private record Snapshot(List<Card> stockPile, List<Card> wastePile, Map<Suit, List<Card>> foundationPiles, List<List<Card>> tableauPiles) {

        private static Snapshot create(SolitaireGame game) {
            return new Snapshot(
                game.stockPile.stream().map(Card::copy).collect(Collectors.toList()),
                game.wastePile.stream().map(Card::copy).collect(Collectors.toList()),
                game.foundationPiles.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().stream().map(Card::copy).collect(Collectors.toList()))),
                game.tableauPiles.stream().map(g -> g.stream().map(Card::copy).collect(Collectors.toList())).toList()
            );
        }

        //This basically prints the entire game in a string so we can compare snapshots.
        //Yes this is a horrible and lazy solution but who cares, it's a minecraft mod, fix it yourself if you are angry at it 😡.
        private String state() {
            StringBuilder str = new StringBuilder();
            str.append(stockPile.size());
            str.append("-");
            stockPile.forEach(c -> str.append(c.toString()));
            str.append("-");

            str.append(wastePile.size());
            str.append("-");
            wastePile.forEach(c -> str.append(c.toString()));
            str.append("-");

            foundationPiles.forEach((suit, cards) -> {
                str.append(suit);
                str.append("-");
                str.append(cards.size());
                str.append("-");
                cards.forEach(c -> str.append(c.toString()));
                str.append("-");
            });

            for(int i = 0; i < tableauPiles.size(); i++) {
                List<Card> cards = tableauPiles.get(i);
                str.append(i);
                str.append("-");
                str.append(cards.size());
                str.append("-");
                cards.forEach(c -> str.append(c.toString()));
                str.append("-");
            }

            return str.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Snapshot snapshot = (Snapshot) o;
            return snapshot.state().equals(this.state());
        }

        @Override
        public int hashCode() {
            return this.state().hashCode();
        }

        private void restore(SolitaireGame game) {
            game.stockPile.setCards(this.stockPile);
            game.wastePile.setCards(this.wastePile);
            game.foundationPiles.forEach((suit, slot) -> {
                slot.setCards(this.foundationPiles.get(suit));
            });
            for(int i = 0; i < game.tableauPiles.size(); i++) {
                game.tableauPiles.get(i).setCards(this.tableauPiles.get(i));
            }
        }



    }

}
