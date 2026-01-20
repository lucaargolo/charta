package dev.lucaargolo.charta.entity;

import com.google.common.collect.ImmutableSet;
import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.registry.ModRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;

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

}
