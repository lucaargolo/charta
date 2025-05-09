package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsMenu;
import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredHolder;
import net.minecraftforge.registries.DeferredRegister;

public class ModMenus {

    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, Charta.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CrazyEightsMenu>> CRAZY_EIGHTS = CONTAINERS.register("crazy_eights", () -> new MenuType<>((IContainerFactory<CrazyEightsMenu>) CrazyEightsMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<FunMenu>> FUN = CONTAINERS.register("fun", () -> new MenuType<>((IContainerFactory<FunMenu>) FunMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<SolitaireMenu>> SOLITAIRE = CONTAINERS.register("solitaire", () -> new MenuType<>((IContainerFactory<SolitaireMenu>) SolitaireMenu::new, FeatureFlags.VANILLA_SET));


    public static void register(IEventBus bus) {
        CONTAINERS.register(bus);
    }

}
