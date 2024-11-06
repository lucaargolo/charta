package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CardPlayer {


    List<Card> getHand();

    void handUpdated();

    CompletableFuture<Card> getPlay(CardGame<?> game);

    void setPlay(CompletableFuture<Card> play);

    void tick(CardGame<?> game);

    boolean shouldCompute();

    void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck);

    void sendMessage(Component message);

    void sendTitle(Component title, @Nullable Component subtitle);

    Component getName();

    DyeColor getColor();

    int getId();

    default CardPlayerHead getHead() {
        return CardPlayerHead.UNKNOWN;
    };

}
