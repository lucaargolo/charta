package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.network.CardTableSelectGamePayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CardTableScreen extends Screen {

    private final BlockPos pos;
    private final CardDeck deck;
    private final int[] players;

    public CardTableScreen(BlockPos pos, CardDeck deck, int[] players) {
        super(Component.empty());
        this.pos = pos;
        this.deck = deck;
        this.players = players;
    }

    @Override
    protected void init() {
        super.init();
        AtomicInteger counter = new AtomicInteger();
        this.addRenderableWidget(new StringWidget(width/2 - 80, height/2 - 100, 160, 20, Component.literal("Please choose a game to play: "), font));
        CardGames.getGames().forEach((gameId, gameFactory) -> {
            CardGame<?> game = gameFactory.create(List.of(), deck);
            boolean invalidDeck = !CardGame.canPlayGame(game, deck);
            boolean notEnoughPlayers = players.length < game.getMinPlayers();
            boolean tooManyPlayers = players.length > game.getMaxPlayers();
            Component name = Component.translatable(gameId.toLanguageKey());
            Button widget = Button.builder(name, button -> {
                PacketDistributor.sendToServer(new CardTableSelectGamePayload(pos, gameId));
                onClose();
            }).bounds(width/2 - 80, height/2 - 80 +counter.get()*25, 160, 20).build();
            widget.active = !(invalidDeck || notEnoughPlayers || tooManyPlayers);
            Tooltip tooltip;
            if(invalidDeck) {
                tooltip = Tooltip.create(Component.literal("You can't play this game using this deck. Try finding another one."));
            }else if(notEnoughPlayers) {
                tooltip = Tooltip.create(Component.literal("There are not enough players for this game. (Minimum of "+game.getMinPlayers()+")"));
            }else if(tooManyPlayers) {
                tooltip = Tooltip.create(Component.literal("There are too many players for this game. (Maximum of"+game.getMaxPlayers()+")"));
            }else{
                tooltip = null;
            }
            widget.setTooltip(tooltip);
            this.addRenderableWidget(widget);
            counter.getAndIncrement();
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
