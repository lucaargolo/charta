package dev.lucaargolo.charta.common.game;

import dev.lucaargolo.charta.common.ChartaMod;
import dev.lucaargolo.charta.common.game.api.game.GameType;
import dev.lucaargolo.charta.common.game.impl.crazyeights.CrazyEightsGame;
import dev.lucaargolo.charta.common.game.impl.crazyeights.CrazyEightsMenu;
import dev.lucaargolo.charta.common.game.impl.fun.FunGame;
import dev.lucaargolo.charta.common.game.impl.fun.FunMenu;
import dev.lucaargolo.charta.common.game.impl.solitaire.SolitaireGame;
import dev.lucaargolo.charta.common.game.impl.solitaire.SolitaireMenu;
import dev.lucaargolo.charta.common.registry.ModRegistry;
import dev.lucaargolo.charta.common.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class Games {

    public static final ResourceKey<Registry<GameType<?, ?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ChartaMod.id("game_type"));
    public static final ModRegistry<GameType<?, ?>> MOD_REGISTRY = ChartaMod.registry(REGISTRY_KEY);

    public static final MinecraftEntry<GameType<CrazyEightsGame, CrazyEightsMenu>> CRAZY_EIGHTS = MOD_REGISTRY.register("crazy_eights", () -> CrazyEightsGame::new);
    public static final MinecraftEntry<GameType<FunGame, FunMenu>> FUN = MOD_REGISTRY.register("fun", () -> FunGame::new);
    public static final MinecraftEntry<GameType<SolitaireGame, SolitaireMenu>> SOLITAIRE = MOD_REGISTRY.register("solitaire", () -> SolitaireGame::new);

    public static Registry<GameType<?, ?>> getRegistry() {
        return MOD_REGISTRY.getRegistry();
    }


}
