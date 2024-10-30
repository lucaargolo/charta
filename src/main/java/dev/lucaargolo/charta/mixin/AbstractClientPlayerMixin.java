package dev.lucaargolo.charta.mixin;

import com.mojang.authlib.GameProfile;
import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Card;
import dev.lucaargolo.charta.game.CardHolderMixed;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements CardHolderMixed {

    public AbstractClientPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Override
    public Collection<Card> charta_getHand() {
        return this.entityData.get(Charta.DATA_CHARTA_HAND);
    }
}
