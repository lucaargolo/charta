package dev.lucaargolo.charta.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.Deck;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.network.CardTableSelectGamePayload;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.TickableWidget;
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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TableScreen extends Screen {

    private final BlockPos pos;
    private final Deck deck;
    private final int[] players;

    private GameWidget<?> widget;

    public TableScreen(BlockPos pos, Deck deck, int[] players) {
        super(Component.translatable("message.charta.choose_game"));
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
        if(this.minecraft != null) {
            guiGraphics.drawCenteredString(font, Component.translatable("message.charta.hold_to_options", this.minecraft.options.keyShift.getKey().getDisplayName()), width / 2, height - 20, 0xFFFFFFFF);
        }
    }

    @Override
    public void tick() {
        if(this.minecraft != null) {
            int mouseX = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth());
            int mouseY = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight());
            for (GuiEventListener widget : this.children()) {
                if (widget instanceof TickableWidget tickable) {
                    tickable.tick(mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public class Game<G extends CardGame<G>> extends Button implements TickableWidget {

        private final ResourceLocation gameId;
        private final CardGames.Factory<G> gameFactory;

        private final ResourceLocation texture;
        private final G game;

        @Nullable
        private final Component tooltip;

        private float lastInset = 0f;
        private float inset = 0f;
        private float lastFov = 30f;
        private float fov = 37f;

        public Game(ResourceLocation gameId, CardGames.Factory<G> gameFactory) {
            super(0, 0, 70, 70, Component.translatable(gameId.toLanguageKey()), (button) -> {
                PacketDistributor.sendToServer(new CardTableSelectGamePayload(pos, gameId, ChartaClient.LOCAL_OPTIONS.getOrDefault(gameId, new byte[0])));
                onClose();
            }, Button.DEFAULT_NARRATION);

            this.gameId = gameId;
            this.gameFactory = gameFactory;
            this.texture = gameId.withPrefix("textures/gui/game/").withSuffix(".png");
            this.game = gameFactory.create(List.of(), Deck.EMPTY);

            List<CardPlayer> cardPlayers = new ArrayList<>();
            for(int entityId : players) {
                if(minecraft != null && minecraft.level != null && minecraft.level.getEntity(entityId) instanceof LivingEntityMixed mixed) {
                    cardPlayers.add(mixed.charta_getCardPlayer());
                }
            }
            Either<CardGame<?>, Component> either = this.game.playerPredicate(cardPlayers);

            boolean invalidDeck = !CardGame.isValidDeck(this.game, deck);
            boolean notEnoughPlayers = players.length < this.game.getMinPlayers();
            boolean tooManyPlayers = players.length > this.game.getMaxPlayers();
            boolean invalidPlayers = either.right().isPresent();

            this.active = !(invalidDeck || notEnoughPlayers || tooManyPlayers || invalidPlayers);
            if(invalidDeck) {
                this.tooltip = Component.translatable("message.charta.cant_play_deck").append(" ").append(Component.translatable("message.charta.try_finding_another"));
            }else if(notEnoughPlayers) {
                this.tooltip = Component.translatable("message.charta.not_enough_players", this.game.getMinPlayers());
            }else if(tooManyPlayers) {
                this.tooltip = Component.translatable("message.charta.too_many_players", this.game.getMaxPlayers());
            }else if(invalidPlayers) {
                this.tooltip = either.right().orElseThrow();
            }else{
                this.tooltip = null;
            }

        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if(this.isHovered() && isShiftDown()) {
                this.isHovered = false;
            }
            if(this.active) {
                float inset = Mth.lerp(partialTick, this.lastInset, this.inset);
                float fov = Mth.lerp(partialTick, this.lastFov, this.fov);

                ChartaClient.CARD_INSET.accept(inset);
                ChartaClient.CARD_FOV.accept(fov);
                ChartaClient.CARD_X_ROT.accept(0f);
                ChartaClient.CARD_Y_ROT.accept(0f);

                float xOffset = (this.getWidth()*1.333333f - this.getWidth())/2f;
                float yOffset = (this.getHeight()*1.333333f - this.getHeight())/2f;

                ChartaGuiGraphics.blitPerspective(guiGraphics, this.texture, this.getX()-xOffset, this.getY()-yOffset, this.getWidth()+(xOffset*2f), this.getHeight()+(yOffset*2f));

                this.lastInset = inset;
                this.lastFov = fov;
            }else{
                ChartaGuiGraphics.blitGrayscale(guiGraphics, this.texture, this.getX(), this.getY(), 70, 70);
            }
            if(this.isHovered()) {
                if(this.active) {
                    guiGraphics.fill(this.getX(), this.getY(), this.getX()+70, this.getY()+70, 0x33FFFFFF);
                }else{
                    guiGraphics.fill(this.getX()+2, this.getY()+2, this.getX()+68, this.getY()+68, 0x33FFFFFF);
                }
            }else if(isShiftDown()) {
                guiGraphics.fill(this.getX()+2, this.getY()+2, this.getX()+68, this.getY()+68, 0x66000000);
            }
        }

        @Override
        public void tick(int mouseX, int mouseY) {
            this.inset = this.isHovered() ? -30f : 0;
            this.fov = 37f;
        }

    }

    public class GameRow<G extends CardGame<G>> extends ContainerObjectSelectionList.Entry<GameRow<G>> {

        protected List<Game<G>> games = new ArrayList<>();
        protected List<Button> plays = new ArrayList<>();
        protected List<Button> configs = new ArrayList<>();

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int i = 0;
            for(Game<G> game : games) {
                game.setX(left + i*75);
                game.setY(top);
                game.render(guiGraphics, mouseX, mouseY, partialTick);
                if(game.isHovered() && game.tooltip != null) {
                    setTooltipForNextRenderPass(game.tooltip);
                }
                i++;
            }
            if(isShiftDown()) {
                i = 0;
                for(Button button : plays) {
                    button.setX(left + i*75 + 25 - 12);
                    button.setY(top + 25);
                    button.render(guiGraphics, mouseX, mouseY, partialTick);
                    i++;
                }
                i = 0;
                for(Button button : configs) {
                    button.setX(left + i*75 + 25 + 12);
                    button.setY(top + 25);
                    button.render(guiGraphics, mouseX, mouseY, partialTick);
                    i++;
                }
            }
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return isShiftDown() ? Stream.concat(plays.stream(), configs.stream()).toList() : games;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return isShiftDown() ? Stream.concat(plays.stream(), configs.stream()).toList() : games;
        }

    }

    public class GameWidget<G extends CardGame<G>> extends ContainerObjectSelectionList<GameRow<G>> implements TickableWidget {

        private final int amount;

        public GameWidget(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 75);
            int margin = width - 40;
            this.amount = margin/75;
        }

        public void addEntry(@NotNull Game<G> entry) {
            if(this.children().isEmpty() || this.children().getLast().games.size() >= amount) {
                this.addEntry(new GameRow<>());
            }
            this.children().getLast().games.add(entry);

            Component play = Component.literal("\ue037").withStyle(ChartaMod.SYMBOLS);
            Button playWidget = Button.builder(play, button -> entry.onPress()).bounds(0, 0, 20, 20).build();
            playWidget.active = entry.active;
            playWidget.setTooltip(Tooltip.create(entry.tooltip != null ? entry.tooltip : Component.translatable("message.charta.play_game")));
            this.children().getLast().plays.add(playWidget);

            Component config = Component.literal("\uE8B8").withStyle(ChartaMod.SYMBOLS);
            Button configWidget = Button.builder(config, button -> {
                minecraft.setScreen(new OptionsScreen<>(TableScreen.this, pos, entry.game, entry.gameId, entry.gameFactory, false));
            }).bounds(0, 0, 20, 20).build();
            configWidget.active = entry.active && !entry.game.getOptions().isEmpty();
            configWidget.setTooltip(Tooltip.create(Component.translatable("message.charta.configure_game")));
            this.children().getLast().configs.add(configWidget);
        }

        @Override
        public int getRowWidth() {
            return amount * 75 - 5;
        }

        @Override
        public void tick(int mouseX, int mouseY) {
            for (GameRow<G> row : this.children()) {
                for(Game<G> game : row.games) {
                    game.tick(mouseX, mouseY);
                }
            }
        }
    }

    private boolean isShiftDown() {
        assert minecraft != null;
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), minecraft.options.keyShift.getKey().getValue());
    }

}
