package dev.lucaargolo.charta.compat;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsScreen;
import dev.lucaargolo.charta.game.fun.FunScreen;
import dev.lucaargolo.charta.game.solitaire.SolitaireScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class JEICompat implements IModPlugin {

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(CrazyEightsScreen.class, new NoHandler<>());
        registration.addGuiScreenHandler(FunScreen.class, new NoHandler<>());
        registration.addGuiScreenHandler(SolitaireScreen.class, new NoHandler<>());
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ChartaMod.id("jei_compat");
    }

    private static class NoHandler<T extends Screen> implements IScreenHandler<T> {
        @Override
        public @Nullable IGuiProperties apply(@NotNull T guiScreen) {
            return null;
        }
    }

}
