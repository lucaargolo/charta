package dev.lucaargolo.charta.datagen;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.game.Deck;
import dev.lucaargolo.charta.item.ModDataComponentTypes;
import dev.lucaargolo.charta.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;


public class ModChestLootProvider implements LootTableSubProvider {

    public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_DECKS =
            ResourceKey.create(Registries.LOOT_TABLE, Charta.id("chests/abandoned_mineshaft_decks"));
    public static final ResourceKey<LootTable> DESERT_PYRAMID_DECKS =
            ResourceKey.create(Registries.LOOT_TABLE, Charta.id("chests/desert_pyramid_decks"));
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_DECKS =
            ResourceKey.create(Registries.LOOT_TABLE, Charta.id("chests/simple_dungeon_decks"));

    @SuppressWarnings("unused")
    public ModChestLootProvider(HolderLookup.Provider provider) {

    }

    @Override
    public void generate(@NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        LootPool.Builder mineshaftPool = LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .add(deck(DefaultDecks.FUN_INVERTED, 0.75))
            .add(deck(DefaultDecks.FUN_CLASSIC, 0.75))
            .add(deck(DefaultDecks.FUN_NEON, 0.25))
            .add(deck(DefaultDecks.FUN_MINIMAL_NEON, 0.25))
            .add(EmptyLootItem.emptyItem().setWeight(50));
        addGroup(mineshaftPool, "metals", 1.25);
        addGroup(mineshaftPool, "gems", 1.0);
        addGroup(mineshaftPool, "neon", 0.25);
        consumer.accept(ABANDONED_MINESHAFT_DECKS, LootTable.lootTable().withPool(mineshaftPool));

        LootPool.Builder pyramidPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(deck(DefaultDecks.FUN_INVERTED, 2.0))
                .add(deck(DefaultDecks.FUN_CLASSIC, 2.0))
                .add(deck(DefaultDecks.FUN_NEON, 0.75))
                .add(deck(DefaultDecks.FUN_MINIMAL_NEON, 0.75))
                .add(EmptyLootItem.emptyItem().setWeight(50));
        addGroup(pyramidPool, "metals", 0.25);
        addGroup(pyramidPool, "neon", 0.5);
        consumer.accept(DESERT_PYRAMID_DECKS, LootTable.lootTable().withPool(pyramidPool));

        LootPool.Builder dungeonPool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(deck(DefaultDecks.FUN_INVERTED, 0.5))
                .add(deck(DefaultDecks.FUN_CLASSIC, 0.5))
                .add(deck(DefaultDecks.FUN_NEON, 0.125))
                .add(deck(DefaultDecks.FUN_MINIMAL_NEON, 0.125))
                .add(EmptyLootItem.emptyItem().setWeight(50));
        addGroup(dungeonPool, "flags", 2.0);
        addGroup(dungeonPool, "metals", 0.5);
        addGroup(dungeonPool, "gems", 0.25);
        addGroup(dungeonPool, "neon", 0.25);
        consumer.accept(SIMPLE_DUNGEON_DECKS, LootTable.lootTable().withPool(dungeonPool));
    }

    private static void addGroup(LootPool.Builder pool, String group, double chanceMultiplier) {
        DefaultDecks.GROUPS.getOrDefault(group, List.of()).forEach(key -> Optional.ofNullable(DefaultDecks.DECKS.get(key)).ifPresent(deck -> pool.add(deck(deck, chanceMultiplier))));
    }

    private static LootPoolSingletonContainer.Builder<?> deck(Deck deck, double chanceMultiplier) {
        ResourceLocation id = DefaultDecks.DECKS.entrySet().stream().filter(e -> e.getValue().equals(deck)).map(Map.Entry::getKey).findFirst().orElse(Charta.id("missing"));
        return LootItem.lootTableItem(ModItems.DECK.get())
                .apply(SetComponentsFunction.setComponent(ModDataComponentTypes.CARD_DECK.get(), id))
                .setWeight(Mth.ceil((3 - deck.getRarity().ordinal()) * 20 * chanceMultiplier));
    }

}
