package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.client.ChartaClient;
import dev.lucaargolo.charta.game.CardDeck;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.game.GameOption;
import dev.lucaargolo.charta.network.CardTableSelectGamePayload;
import dev.lucaargolo.charta.network.PlayerOptionsPayload;
import dev.lucaargolo.charta.utils.ChartaGuiGraphics;
import dev.lucaargolo.charta.utils.CustomOptionTooltip;
import dev.lucaargolo.charta.utils.PacketUtils;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class OptionsScreen<G extends CardGame<G>> extends Screen {

    private final Screen parent;
    private final BlockPos pos;
    private final G game;

    private final ResourceLocation gameId;
    private final CardGames.Factory<G> gameFactory;
    private final boolean showcase;

    private OptionsWidget widget;
    private Button resetButton;
    private Button saveButton;

    public OptionsScreen(Screen parent, BlockPos pos, G game, ResourceLocation gameId, CardGames.Factory<G> gameFactory, boolean showcase) {
        super(Component.translatable("message.charta.this_game_options", Component.translatable(gameId.toLanguageKey())));
        this.parent = parent;
        this.pos = pos;
        this.game = game;

        this.gameId = gameId;
        this.gameFactory = gameFactory;
        this.showcase = showcase;

        if(!showcase) {
            Optional.ofNullable(ChartaClient.LOCAL_OPTIONS.get(this.gameId)).ifPresent(game::setRawOptions);
        }
    }

    @Override
    protected void init() {
        this.widget = this.addRenderableWidget(new OptionsWidget(minecraft, width, height, 30));

        for(int i = 0; i < this.game.getOptions().size(); i++) {
            GameOption<?> option = this.game.getOptions().get(i);
            widget.addEntry(option.getWidget(o -> updateButtons(false), font, widget.getRowWidth(), 20, showcase));
        }

        Component back = Component.literal("\ue5c4").withStyle(Charta.SYMBOLS);
        this.addRenderableWidget(Button.builder(back, b -> this.onClose())
                .bounds(5, 5, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("message.charta.go_back")))
                .build()
        );

        if(!showcase) {
            this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("button.charta.reset"), b -> {
                boolean reset = CardGames.areOptionsChanged(gameFactory, game);
                if(reset) {
                    G defaultGame = gameFactory.create(List.of(), CardDeck.EMPTY);
                    this.game.setRawOptions(defaultGame.getRawOptions());
                }
                this.updateButtons(false);
            }).bounds(width/2 - 108, height-25, 68, 20).tooltip(Tooltip.create(Component.translatable("message.charta.reset_options"))).build());
            this.resetButton.active = CardGames.areOptionsChanged(gameFactory, game);

            this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("button.charta.save"), b -> {
                this.updateButtons(true);
                ChartaClient.LOCAL_OPTIONS.put(gameId, this.game.getRawOptions());
                PacketUtils.sendToServer(new PlayerOptionsPayload(ChartaClient.LOCAL_OPTIONS));
            }).bounds(width/2 - 32, height-25, 68, 20).tooltip(Tooltip.create(Component.translatable("message.charta.save_options"))).build());
            this.saveButton.active = false;

            this.addRenderableWidget(Button.builder(Component.translatable("button.charta.start"), b -> {
                PacketUtils.sendToServer(new CardTableSelectGamePayload(pos, gameId, this.game.getRawOptions()));
                onClose();
            }).bounds(width/2 + 44, height-25, 68, 20).tooltip(Tooltip.create(Component.translatable("message.charta.start_options"))).build());
        }

        updateButtons(true);
    }

    public void updateButtons(boolean saved) {
        boolean reset = false;
        G defaultGame = gameFactory.create(List.of(), CardDeck.EMPTY);
        for(int i = 0; i < defaultGame.getOptions().size(); i++) {
            GameOption<?> defaultOption = defaultGame.getOptions().get(i);
            GameOption<?> modifiedOption = game.getOptions().get(i);
            GameOption.Widget entry = this.widget.getEntry(i);
            entry.setTooltip(new CustomOptionTooltip(entry.getTooltip(), defaultOption.get().toString(), modifiedOption.get().toString()));
            if(modifiedOption.getValue() != defaultOption.getValue()) {
                this.widget.changed.set(i, true);
                reset = true;
            }else{
                this.widget.changed.set(i, false);
            }
        }
        if(this.resetButton != null)
            this.resetButton.active = reset;
        if(this.saveButton != null)
            this.saveButton.active = !saved;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, title, width/2, 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if(this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public class OptionsWidget extends ContainerObjectSelectionList<GameOption.Widget> {

        protected final BooleanList changed = new BooleanArrayList();

        public OptionsWidget(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, height-y, 25);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected void renderBackground(@NotNull GuiGraphics pGuiGraphics) {
            ChartaGuiGraphics.renderBackgroundBlur(OptionsScreen.this, pGuiGraphics, 0.5f);
            pGuiGraphics.fill(this.x0, this.y0-2, this.x1, this.y0-1, 0x55FFFFFF);
            pGuiGraphics.fill(this.x0, this.y0-1, this.x1, this.y0, 0xAA000000);
            pGuiGraphics.fill(this.x0, this.y0, this.x1, this.y1, 0x66000000);
            pGuiGraphics.fill(this.x0, this.y1, this.x1, this.y1+1, 0xAA000000);
            pGuiGraphics.fill(this.x0, this.y1+1, this.x1, this.y1+2, 0x55FFFFFF);
        }

        @Override
        protected void renderItem(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
            super.renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, width, height);
            if(changed.getBoolean(index)) {
                guiGraphics.drawString(minecraft.font, "!", left + width - 4, top + 2, (Util.getMillis() / 1000) % 2 == 0 ? 0xFF0000 : 0xFFFF00);
            }
        }

        @Override
        public int addEntry(@NotNull GameOption.Widget entry) {
            changed.add(false);
            return super.addEntry(entry);
        }

        @Override
        public GameOption.@NotNull Widget getEntry(int index) {
            return super.getEntry(index);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 8;
        }

    }

}
