package dev.lucaargolo.charta.entity;

import dev.lucaargolo.charta.Charta;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Charta.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = ENTITY_TYPES.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
        .sized(0f, 0f)
        .build("seat")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<IronLeashFenceKnotEntity>> IRON_LEASH_KNOT = ENTITY_TYPES.register("iron_leash_knot", () -> EntityType.Builder.<IronLeashFenceKnotEntity>of(IronLeashFenceKnotEntity::new, MobCategory.MISC)
        .noSave()
        .sized(0.375F, 0.5F)
        .eyeHeight(0.0625F)
        .clientTrackingRange(10)
        .updateInterval(Integer.MAX_VALUE)
        .build("iron_leash_knot")
    );

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }


}
