package dev.lucaargolo.charta.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@EventBusSubscriber(modid = Charta.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class ModVillagerProfessions {

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Charta.MOD_ID);

    public static final RegistryObject<VillagerProfession> DEALER = VILLAGER_PROFESSIONS.register("dealer", () -> {
        assert ModPoiTypes.DEALER.getKey() != null;
        return new VillagerProfession(
                "dealer",
                heldJob -> heldJob.is(ModPoiTypes.DEALER.getKey()),
                acquirableJob -> acquirableJob.is(ModPoiTypes.DEALER.getKey()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_CARTOGRAPHER
        );
    });

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

    public static void register(IEventBus bus) {
        VILLAGER_PROFESSIONS.register(bus);
    }

}
