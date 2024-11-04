package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.*;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.utils.ModEntityDataSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityMixed {

    @Unique
    private final CardPlayer charta_cardPlayer = new AutoPlayer(random.nextFloat()) {

        @Override
        public void handUpdated() {
            entityData.set(Charta.ENTITY_HAND, getHand());
        }

        @Override
        public void tick(CardGame<?> game) {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            if(!(living instanceof ServerPlayer)) {
                super.tick(game);
            }
        }

        @Override
        public void openScreen(CardGame<?> game, BlockPos pos, CardDeck deck) {
            LivingEntity living = (LivingEntity) (Object) LivingEntityMixin.this;
            if(living instanceof ServerPlayer serverPlayer) {
                game.openScreen(serverPlayer, serverPlayer.serverLevel(), pos, deck);
            }
        }

        @Override
        public ResourceLocation getTexture() {
            return null;
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
    public CardPlayer charta_getCardPlayer() {
        return charta_cardPlayer;
    }

}
