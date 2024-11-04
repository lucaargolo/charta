package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.Charta;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardGames {

    private static final Map<ResourceLocation, CardGameFactory<?>> factories = new HashMap<>();

    public static final CardGameFactory<CrazyEightsGame> CRAZY_EIGHTS = register(Charta.id("crazy_eights"), CrazyEightsGame::new);

    public static <G extends CardGame> CardGameFactory<G> register(ResourceLocation location, CardGameFactory<G> factory) {
        if(factories.containsKey(location)) {
            throw new IllegalArgumentException("Duplicate key: " + location);
        }else{
            factories.put(location, factory);
            return factory;
        }
    }

    public static Map<ResourceLocation, CardGameFactory<?>> getGames() {
        return factories;
    }

    @Nullable
    public static CardGameFactory<?> getGame(ResourceLocation gameId) {
        return factories.get(gameId);
    }

    @FunctionalInterface
    public interface CardGameFactory<G extends CardGame> {

        G create(List<CardPlayer> players, CardDeck deck);

    }

}
