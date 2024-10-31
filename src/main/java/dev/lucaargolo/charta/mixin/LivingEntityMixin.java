package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayer;
import dev.lucaargolo.charta.game.CardPlayerMixed;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements CardPlayerMixed {

    @Unique
    private final List<Card> charta_hand = new ArrayList<>();
    @Unique
    private CompletableFuture<Card> charta_play = new CompletableFuture<>();
    @Unique
    private CardPlayer charta_cardPlayer = new CardPlayer() {
        @Override
        public CompletableFuture<Card> getPlay(CardGame game) {
            return charta_getPlay(game);
        }

        @Override
        public void setPlay(CompletableFuture<Card> play) {
            charta_setPlay(play);
        }

        @Override
        public List<Card> getHand() {
            return charta_getHand();
        }

        @Override
        public void handUpdated() {
            charta_handUpdated();
        }

        @Override
        public ResourceLocation getTexture() {
            return charta_getTexture();
        }
    };

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void init(CallbackInfo ci) {
        Charta.ENTITY_HAND = SynchedEntityData.defineId(LivingEntity.class, ModEntityDataSerializers.CARD_LIST);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(Charta.ENTITY_HAND, new ArrayList<>());
    }

    @Override
    public List<Card> charta_getHand() {
        return charta_hand;
    }

    @Override
    public void charta_handUpdated() {
        this.entityData.set(Charta.ENTITY_HAND, charta_hand);
    }

    @Override
    public ResourceLocation charta_getTexture() {
        return null;
    }

    @Override
    public CompletableFuture<Card> charta_getPlay(CardGame game) {
        return this.charta_play;
    }

    @Override
    public void charta_setPlay(CompletableFuture<Card> play) {
        this.charta_play = play;
    }

    @Override
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

}
