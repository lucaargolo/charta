package dev.lucaargolo.charta.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.registry.ModRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;

public class ModVillagerProfessions {

    public static final ModRegistry<VillagerProfession> REGISTRY = ChartaMod.registry(Registries.VILLAGER_PROFESSION);

    public static final MinecraftEntry<VillagerProfession> DEALER = REGISTRY.register("dealer", () -> new VillagerProfession(
        "dealer",
        heldJob -> heldJob.is(ModPoiTypes.DEALER.key()),
        acquirableJob -> acquirableJob.is(ModPoiTypes.DEALER.key()),
        ImmutableSet.of(),
        ImmutableSet.of(),
        SoundEvents.VILLAGER_WORK_CARTOGRAPHER
    ));

    @SubscribeEvent
    public static void trades(final VillagerTradesEvent event) {
        if(event.getType() == DEALER.get()) {
            List<VillagerTrades.ItemListing> level1 = event.getTrades().get(1);
            level1.add(ModItemListings.COMMON_DECKS);
            level1.add(ModItemListings.DRINKS);

            List<VillagerTrades.ItemListing> level2 = event.getTrades().get(2);
            level2.add(ModItemListings.UNCOMMON_DECKS);
            level2.add(ModItemListings.IRON_LEAD);

            List<VillagerTrades.ItemListing> level3 = event.getTrades().get(3);
            level3.add(ModItemListings.RARE_DECKS);

            List<VillagerTrades.ItemListing> level4 = event.getTrades().get(4);
            level4.add(ModItemListings.EPIC_DECKS);
        }
    }

}
