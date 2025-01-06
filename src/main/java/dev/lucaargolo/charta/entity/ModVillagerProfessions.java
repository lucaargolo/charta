package dev.lucaargolo.charta.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.Charta;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModVillagerProfessions {

    public static final Map<ResourceLocation, VillagerProfession> VILLAGER_PROFESSIONS = new HashMap<>();

    public static final VillagerProfession DEALER = register("dealer", () -> new VillagerProfession(
        "dealer",
        heldJob -> heldJob.is(Charta.id("dealer")),
        acquirableJob -> acquirableJob.is(Charta.id("dealer")),
        ImmutableSet.of(),
        ImmutableSet.of(),
        SoundEvents.VILLAGER_WORK_CARTOGRAPHER
    ));

    public static void registerVillagerTrades() {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> mutableTrades = new Int2ObjectOpenHashMap<>();

        List<VillagerTrades.ItemListing> level1 = mutableTrades.computeIfAbsent(1, k -> NonNullList.create());
        level1.add(ModItemListings.COMMON_DECKS);
        level1.add(ModItemListings.DRINKS);

        List<VillagerTrades.ItemListing> level2 = mutableTrades.computeIfAbsent(2, k -> NonNullList.create());
        level2.add(ModItemListings.UNCOMMON_DECKS);
        level2.add(ModItemListings.IRON_LEAD);

        List<VillagerTrades.ItemListing> level3 = mutableTrades.computeIfAbsent(3, k -> NonNullList.create());
        level3.add(ModItemListings.RARE_DECKS);

        List<VillagerTrades.ItemListing> level4 = mutableTrades.computeIfAbsent(4, k -> NonNullList.create());
        level4.add(ModItemListings.EPIC_DECKS);

        VillagerTrades.TRADES.put(DEALER, new Int2ObjectOpenHashMap<>(Map.ofEntries(mutableTrades.int2ObjectEntrySet().stream().map(a -> new MutablePair<>(a.getIntKey(), a.getValue().toArray(new VillagerTrades.ItemListing[0])) {}).toList().toArray(new Map.Entry[0]))));
    }

    private static <T extends VillagerProfession> T register(String id, Supplier<T> villagerProfession) {
        T obj = villagerProfession.get();
        VILLAGER_PROFESSIONS.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        VILLAGER_PROFESSIONS.forEach((id, villagerProfession) -> Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, id, villagerProfession));
    }

}
