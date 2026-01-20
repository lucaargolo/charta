package dev.lucaargolo.charta.menu;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsMenu;
import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import dev.lucaargolo.charta.registry.ModMenuTypeRegistry;

public class ModMenuTypes {

    public static final ModMenuTypeRegistry REGISTRY = ChartaMod.menuTypeRegistry();

    public static final ModMenuTypeRegistry.AdvancedMenuTypeEntry<CrazyEightsMenu, CrazyEightsMenu.Definition> CRAZY_EIGHTS = REGISTRY.register("crazy_eights", CrazyEightsMenu::new, CrazyEightsMenu.Definition.STREAM_CODEC);
    public static final ModMenuTypeRegistry.AdvancedMenuTypeEntry<FunMenu, FunMenu.Definition> FUN = REGISTRY.register("fun", FunMenu::new, FunMenu.Definition.STREAM_CODEC);
    public static final ModMenuTypeRegistry.AdvancedMenuTypeEntry<SolitaireMenu, SolitaireMenu.Definition> SOLITAIRE = REGISTRY.register("solitaire", SolitaireMenu::new, SolitaireMenu.Definition.STREAM_CODEC);

}
