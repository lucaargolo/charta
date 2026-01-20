package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.ChartaMod;
import dev.lucaargolo.charta.registry.ModRegistry;
import dev.lucaargolo.charta.registry.minecraft.MinecraftEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {

    public static final ModRegistry<EntityType<?>> REGISTRY = ChartaMod.registry(Registries.ENTITY_TYPE);

    public static final MinecraftEntry<EntityType<SeatEntity>> SEAT = REGISTRY.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
        .sized(0f, 0f)
        .build("seat")
    );
    public static final MinecraftEntry<EntityType<IronLeashFenceKnotEntity>> IRON_LEASH_KNOT = REGISTRY.register("iron_leash_knot", () -> EntityType.Builder.<IronLeashFenceKnotEntity>of(IronLeashFenceKnotEntity::new, MobCategory.MISC)
        .noSave()
        .sized(0.375F, 0.5F)
        .eyeHeight(0.0625F)
        .clientTrackingRange(10)
        .updateInterval(Integer.MAX_VALUE)
        .build("iron_leash_knot")
    );

}
