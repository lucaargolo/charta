package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.utils.CardPlayerHead;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface CardPlayer {

    GameSlot getHand();

    CompletableFuture<CardPlay> getPlay(CardGame<?> game);

    void setPlay(CompletableFuture<CardPlay> play);

    void tick(CardGame<?> game);

    boolean shouldCompute();

    void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck);

    void sendMessage(Component message);

    void sendTitle(Component title, @Nullable Component subtitle);

    Component getName();

    default Component getColoredName() {
        return getName().copy().withColor(getColor().getTextureDiffuseColor());
    }

    DyeColor getColor();

    int getId();

    @Nullable
    default LivingEntity getEntity() {
        return null;
    }

    default CardPlayerHead getHead() {
        return CardPlayerHead.UNKNOWN;
    }

    default void playSound(SoundEvent sound) {
        LivingEntity entity = getEntity();
        if(entity != null)
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, 1f, entity.getRandom().nextFloat() * 0.5f + 0.7f);
    }

}
