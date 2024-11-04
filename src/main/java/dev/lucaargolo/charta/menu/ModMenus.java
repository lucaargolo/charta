package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, Charta.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CrazyEightsMenu>> CRAZY_EIGHTS = CONTAINERS.register("crazy_eights", () -> new MenuType<>((IContainerFactory<CrazyEightsMenu>) CrazyEightsMenu::new, FeatureFlags.VANILLA_SET));


    public static void register(IEventBus bus) {
        CONTAINERS.register(bus);
    }

}
