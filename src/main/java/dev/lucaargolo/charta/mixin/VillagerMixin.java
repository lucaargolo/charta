package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardGame;
import dev.lucaargolo.charta.game.CardPlayerMixed;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(Villager.class)
public abstract class VillagerMixin extends LivingEntity implements CardPlayerMixed {

    @Unique
    private static final EntityDataAccessor<List<Card>> DATA_CHARTA_HAND = SynchedEntityData.defineId(Villager.class, ModEntityDataSerializers.CARD_LIST.get());

    @Unique
    private final List<Card> charta_hand = new ArrayList<>();
    @Unique
    private CompletableFuture<Card> charta_play = new CompletableFuture<>();

    protected VillagerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void setDataChartaHand(CallbackInfo ci) {
        Charta.VILLAGER_HAND = DATA_CHARTA_HAND;
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    public void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_CHARTA_HAND, charta_hand);
    }

    @Override
    public Collection<Card> charta_getHand() {
        return charta_hand;
    }

    @Override
    public void charta_handUpdated() {
        this.entityData.set(DATA_CHARTA_HAND, charta_hand);
    }

    @Override
    public CompletableFuture<Card> charta_getPlay(CardGame game) {
        return this.charta_play;
    }

    @Override
    public void charta_setPlay(CompletableFuture<Card> play) {
        this.charta_play = play;
    }

}
