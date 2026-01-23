package dev.lucaargolo.charta.common.game.impl;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.CardPlayer;
import dev.lucaargolo.charta.common.game.api.GamePlay;
import dev.lucaargolo.charta.common.game.api.card.Card;
import dev.lucaargolo.charta.common.game.api.card.Deck;
import dev.lucaargolo.charta.common.game.api.game.Game;
import dev.lucaargolo.charta.common.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AutoPlayer implements CardPlayer {

    private final Random random = new Random();
    private final LinkedList<Card> hand = new LinkedList<>();
    private CompletableFuture<GamePlay> play = new CompletableFuture<>();
    private int playAge = 0;

    protected final float intelligence;

    public AutoPlayer(float intelligence) {
        this.intelligence = intelligence;
    }

    @Override
    public LinkedList<Card> hand() {
        return hand;
    }

    @Override
    public void play(GamePlay play) {
        this.play.complete(play);
    }

    @Override
    public void afterPlay(Consumer<GamePlay> consumer) {
        this.play.thenAccept(play -> {
            try{
                consumer.accept(play);
            }catch (Exception e) {
                ChartaMod.LOGGER.error("Error while handling {}'s Card Play. ", this.getName().getString(), e);
            }
        });
    }

    @Override
    public void resetPlay() {
        this.play = new CompletableFuture<>();
        this.playAge = 0;
    }

    @Override
    public void tick(Game<?, ?> game) {
        if(game.getCurrentPlayer() == this && !play.isDone()) {
            int threshold = (int) Mth.lerp(intelligence, 50, 20);
            threshold += random.nextInt(-5, 40);
            if(playAge > threshold) {
                GamePlay cardPlay = game.getBestPlay(this);
                play(cardPlay);
            }else{
                playAge++;
            }
        }

    }

    @Override
    public void openScreen(Game<?, ?> game, BlockPos pos, Deck deck) {

    }

    @Override
    public void sendMessage(Component message) {

    }

    @Override
    public void sendTitle(Component title, @Nullable Component subtitle) {

    }

    @Override
    public Component getName() {
        return Component.literal("AutoPlayer");
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public CardPlayerHead getHead() {
        return CardPlayerHead.ROBOT;
    }

    @Override
    public boolean shouldCompute() {
        return true;
    }
}
