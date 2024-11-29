package dev.lucaargolo.charta.game;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.blockentity.ModBlockEntityTypes;
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

    private static final Map<ResourceLocation, CardGameFactory<?>> factories = new HashMap<>();

    public static final CardGameFactory<CrazyEightsGame> CRAZY_EIGHTS = register(Charta.id("crazy_eights"), CrazyEightsGame::new);
    public static final CardGameFactory<FunGame> FUN = register(Charta.id("fun"), FunGame::new);

    public static <G extends CardGame<G>> CardGameFactory<G> register(ResourceLocation location, CardGameFactory<G> factory) {
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

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    public static <G extends CardGame<G>> G getGameForMenu(CardGameFactory<G> factory, ContainerLevelAccess access, CardDeck deck, int[] players) {
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
            return factory.create(cardPlayers, deck);
        }
    }

    @FunctionalInterface
    public interface CardGameFactory<G extends CardGame<G>> {

        G create(List<CardPlayer> players, CardDeck deck);

    }

}
