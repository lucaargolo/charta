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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardGames {

    private static final Map<ResourceLocation, Factory<?, ?>> factories = new HashMap<>();

    public static final Factory<CrazyEightsGame, CrazyEightsMenu> CRAZY_EIGHTS = register(ChartaMod.id("crazy_eights"), CrazyEightsGame::new);
    public static final Factory<FunGame, FunMenu> FUN = register(ChartaMod.id("fun"), FunGame::new);
    public static final Factory<SolitaireGame, SolitaireMenu> SOLITAIRE = register(ChartaMod.id("solitaire"), SolitaireGame::new);

    public static <G extends CardGame<G, M>, M extends AbstractCardMenu<G, M>> Factory<G, M> register(ResourceLocation location, Factory<G, M> factory) {
        if(factories.containsKey(location)) {
            throw new IllegalArgumentException("Duplicate key: " + location);
        }else{
            factories.put(location, factory);
            return factory;
        }
    }

    public static Map<ResourceLocation, Factory<?, ?>> getGames() {
        return factories;
    }

    @Nullable
    public static CardGames.Factory<?, ?> getGame(ResourceLocation gameId) {
        return factories.get(gameId);
    }

    public static ResourceLocation getGameId(CardGames.Factory<?, ?> factory) {
        return factories.entrySet().stream().filter(entry -> entry.getValue() == factory).map(Map.Entry::getKey).findFirst().orElse(ChartaMod.MISSING_GAME);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public static <G extends CardGame<G, M>, M extends AbstractCardMenu<G, M>> G getGameForMenu(Factory<G, M> factory, ContainerLevelAccess access, Deck deck, int[] players, byte[] options) {
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
            G game = factory.create(cardPlayers, deck);
            game.setRawOptions(options);
            return game;
        }
    }

    public static <G extends CardGame<G, M>, M extends AbstractCardMenu<G, M>> boolean areOptionsChanged(Factory<G, M> factory, G game) {
        G defaultGame = factory.create(List.of(), Deck.EMPTY);
        for(int i = 0; i < defaultGame.getOptions().size(); i++) {
            GameOption<?> defaultOption = defaultGame.getOptions().get(i);
            GameOption<?> modifiedOption = game.getOptions().get(i);
            if(modifiedOption.getValue() != defaultOption.getValue()) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Factory<G extends CardGame<G, M>, M extends AbstractCardMenu<G, M>> {

        G create(List<CardPlayer> players, Deck deck);

    }

}
