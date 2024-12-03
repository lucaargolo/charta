package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.network.CardTableSelectGamePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TableScreen extends Screen {

    private final BlockPos pos;
    private final CardDeck deck;
    private final int[] players;

    private GameWidget<?> widget;

    public TableScreen(BlockPos pos, CardDeck deck, int[] players) {
        super(Component.translatable("charta.message.choose_game"));
        this.pos = pos;
        this.deck = deck;
        this.players = players;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void init() {
        super.init();
        this.widget = this.addRenderableWidget(new GameWidget<>(minecraft, width, height-60, 30));
        CardGames.getGames().forEach((gameId, gameFactory) ->  {
            this.widget.addEntry(new Game(gameId, gameFactory));
        });
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width/2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public class Game<G extends CardGame<G>> extends ContainerObjectSelectionList.Entry<Game<G>> {

        private final Button gameWidget;
        private final Button configWidget;

        public Game(ResourceLocation gameId, CardGames.Factory<G> gameFactory) {
            G game = gameFactory.create(List.of(), CardDeck.EMPTY);
            boolean invalidDeck = !CardGame.canPlayGame(game, deck);
            boolean notEnoughPlayers = players.length < game.getMinPlayers();
            boolean tooManyPlayers = players.length > game.getMaxPlayers();
            Component name = Component.translatable(gameId.toLanguageKey());

            this.gameWidget = Button.builder(name, button -> {
                PacketDistributor.sendToServer(new CardTableSelectGamePayload(pos, gameId, ChartaClient.LOCAL_OPTIONS.getOrDefault(gameId, new byte[0])));
                onClose();
            }).bounds(0, 0, 135, 20).build();
            this.gameWidget.active = !(invalidDeck || notEnoughPlayers || tooManyPlayers);
            Tooltip tooltip;
            if(invalidDeck) {
                tooltip = Tooltip.create(Component.translatable("charta.message.cant_play_deck").append(" ").append(Component.translatable("charta.message.try_finding_another")));
            }else if(notEnoughPlayers) {
                tooltip = Tooltip.create(Component.translatable("charta.message.not_enough_players", game.getMinPlayers()));
            }else if(tooManyPlayers) {
                tooltip = Tooltip.create(Component.translatable("charta.message.too_many_players", game.getMaxPlayers()));
            }else{
                tooltip = null;
            }
            this.gameWidget.setTooltip(tooltip);

            Component config = Component.literal("\uE8B8").withStyle(Charta.SYMBOLS);
            this.configWidget = Button.builder(config, button -> {
                if(minecraft != null) {
                    minecraft.setScreen(new OptionsScreen<>(TableScreen.this, pos, game, gameId, gameFactory, false));
                }
            }).bounds(0, 0, 20, 20).build();
            this.configWidget.active = !game.getOptions().isEmpty();
            this.configWidget.setTooltip(Tooltip.create(Component.translatable("charta.message.configure_game")));
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(gameWidget, configWidget);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(gameWidget, configWidget);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            gameWidget.setX(left);
            gameWidget.setY(top);
            gameWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            configWidget.setX(left+140);
            configWidget.setY(top);
            configWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

    }

    public static class GameWidget<G extends CardGame<G>> extends ContainerObjectSelectionList<Game<G>> {

        public GameWidget(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 25);
        }

        @Override
        public int addEntry(@NotNull Game<G> entry) {
            return super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 160;
        }
    }

}
