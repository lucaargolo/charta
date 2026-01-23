package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsGame;
import dev.lucaargolo.charta.game.crazyeights.CrazyEightsMenu;
import dev.lucaargolo.charta.game.fun.FunGame;
import dev.lucaargolo.charta.game.fun.FunMenu;
import dev.lucaargolo.charta.game.solitaire.SolitaireGame;
import dev.lucaargolo.charta.game.solitaire.SolitaireMenu;
import dev.lucaargolo.charta.menu.AbstractCardMenu;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import dev.lucaargolo.charta.registry.ModRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.ArrayList;
import java.util.List;

public class ModGameTypes {

    public static final ResourceKey<Registry<GameType<?, ?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ChartaMod.id("game_type"));
    public static final ModRegistry<GameType<?, ?>> REGISTRY = ChartaMod.registry(REGISTRY_KEY);

    public static final MinecraftEntry<GameType<CrazyEightsGame, CrazyEightsMenu>> CRAZY_EIGHTS = REGISTRY.register("crazy_eights", () -> CrazyEightsGame::new);
    public static final MinecraftEntry<GameType<FunGame, FunMenu>> FUN = REGISTRY.register("fun", () -> FunGame::new);
    public static final MinecraftEntry<GameType<SolitaireGame, SolitaireMenu>> SOLITAIRE = REGISTRY.register("solitaire", () -> SolitaireGame::new);

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public static <G extends Game<G, M>, M extends AbstractCardMenu<G, M>> G getGameForMenu(GameType<G, M> type, ContainerLevelAccess access, Deck deck, int[] players, byte[] options) {
        try{
            return access.evaluate((level, pos) -> level.getBlockEntity(pos, ModBlockEntityTypes.CARD_TABLE.get()).get()).map(table -> (G) table.getGame()).get();
        }catch (Exception e) {
            List<CardPlayer> cardPlayers = new ArrayList<>();
            for (int entityId : players) {
                CardPlayer player = access.evaluate((level, pos) -> {
                    if (entityId >= 0) {
                        Entity entity = level.getEntity(entityId);
                        if (entity instanceof LivingEntityMixed mixed) {
                            return mixed.charta_getCardPlayer();
                        }
                    }
                    return new AutoPlayer(1f);
                }).orElse(new AutoPlayer(1f));
                cardPlayers.add(player);
            }
            G game = type.create(cardPlayers, deck);
            game.setRawOptions(options);
            return game;
        }
    }

    public static <G extends Game<G, M>, M extends AbstractCardMenu<G, M>> boolean areOptionsChanged(GameType<G, M> type, G game) {
        G defaultGame = type.create(List.of(), Deck.EMPTY);
        for(int i = 0; i < defaultGame.getOptions().size(); i++) {
            GameOption<?> defaultOption = defaultGame.getOptions().get(i);
            GameOption<?> modifiedOption = game.getOptions().get(i);
            if(modifiedOption.getValue() != defaultOption.getValue()) {
                return true;
            }
        }
        return false;
    }


}
