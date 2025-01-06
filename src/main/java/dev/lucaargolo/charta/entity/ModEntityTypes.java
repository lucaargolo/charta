package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModEntityTypes {

    public static final Map<ResourceLocation, EntityType<?>> ENTITY_TYPES = new HashMap<>();

    public static final EntityType<SeatEntity> SEAT = register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
        .sized(0f, 0f)
        .build("seat")
    );
    public static final EntityType<IronLeashFenceKnotEntity> IRON_LEASH_KNOT = register("iron_leash_knot", () -> EntityType.Builder.<IronLeashFenceKnotEntity>of(IronLeashFenceKnotEntity::new, MobCategory.MISC)
        .noSave()
        .sized(0.375F, 0.5F)
        .eyeHeight(0.0625F)
        .clientTrackingRange(10)
        .updateInterval(Integer.MAX_VALUE)
        .build("iron_leash_knot")
    );

    private static <E extends Entity, T extends EntityType<E>> T register(String id, Supplier<T> entityType) {
        T obj = entityType.get();
        ENTITY_TYPES.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        ENTITY_TYPES.forEach((id, entityType) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id, entityType));
    }

}
