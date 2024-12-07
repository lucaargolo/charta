package dev.lucaargolo.charta.mixin;

import dev.lucaargolo.charta.client.gui.screens.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "isChatFocused", cancellable = true)
    public void isChatFocused(CallbackInfoReturnable<Boolean> cir) {
        if(this.minecraft.screen instanceof GameScreen<?,?>) {
            cir.setReturnValue(true);
        }
    }

}
