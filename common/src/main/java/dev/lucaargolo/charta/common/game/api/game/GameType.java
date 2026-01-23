package dev.lucaargolo.charta.common.game.api.game;

import dev.lucaargolo.charta.common.block.entity.ModBlockEntityTypes;
import dev.lucaargolo.charta.common.game.api.CardPlayer;
import dev.lucaargolo.charta.common.game.api.card.Deck;
import dev.lucaargolo.charta.common.game.impl.AutoPlayer;
import dev.lucaargolo.charta.common.menu.AbstractCardMenu;
import dev.lucaargolo.charta.mixed.LivingEntityMixed;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerLevelAccess;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface GameType<G extends Game<G, M>, M extends AbstractCardMenu<G, M>> {

    G create(List<CardPlayer> players, Deck deck);

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
    static <G extends Game<G, M>, M extends AbstractCardMenu<G, M>> G getGameForMenu(GameType<G, M> type, ContainerLevelAccess access, Deck deck, int[] players, byte[] options) {
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

    static <G extends Game<G, M>, M extends AbstractCardMenu<G, M>> boolean areOptionsChanged(GameType<G, M> type, G game) {
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
