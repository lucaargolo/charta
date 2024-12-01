package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class AutoPlayer implements CardPlayer {

    private final Random random = new Random();
    private final GameSlot hand = new GameSlot();
    private CompletableFuture<CardPlay> play = new CompletableFuture<>();
    private int playAge = 0;

    protected final float intelligence;

    public AutoPlayer(float intelligence) {
        this.intelligence = intelligence;
    }

    @Override
    public GameSlot getHand() {
        return hand;
    }

    @Override
    public CompletableFuture<CardPlay> getPlay(CardGame<?> game) {
        return play;
    }

    @Override
    public void setPlay(CompletableFuture<CardPlay> play) {
        this.play = play;
        this.playAge = 0;
    }

    @Override
    public void tick(CardGame<?> game) {
        if(game.getCurrentPlayer() == this && !play.isDone()) {
            int threshold = (int) Mth.lerp(intelligence, 50, 20);
            threshold += random.nextInt(-5, 40);
            if(playAge > threshold) {
                CardPlay cardPlay = game.getBestPlay(this);
                play.complete(cardPlay);
            }else{
                playAge++;
            }
        }

    }

    @Override
    public void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck) {

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
