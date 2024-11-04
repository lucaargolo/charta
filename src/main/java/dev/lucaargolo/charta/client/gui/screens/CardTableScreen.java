package dev.lucaargolo.charta.client.gui.screens;

import dev.lucaargolo.charta.game.CardGames;
import dev.lucaargolo.charta.network.CardTableSelectGamePayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicInteger;

public class CardTableScreen extends Screen {

    private final BlockPos pos;

    public CardTableScreen(BlockPos pos) {
        super(Component.empty());
        this.pos = pos;
    }

    @Override
    protected void init() {
        super.init();
        AtomicInteger counter = new AtomicInteger();
        this.addRenderableWidget(new StringWidget(width/2 - 80, height/2 - 100, 160, 20, Component.literal("Please choose a game to play: "), font));
        CardGames.getGames().forEach((gameId, gameFactory) -> {
            Component name = Component.translatable(gameId.toLanguageKey());
            this.addRenderableWidget(Button.builder(name, button -> {
                PacketDistributor.sendToServer(new CardTableSelectGamePayload(pos, gameId));
                onClose();
            }).bounds(width/2 - 80, height/2 - 80 +counter.get()*25, 160, 20).build());
            counter.getAndIncrement();
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
