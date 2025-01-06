package dev.lucaargolo.charta.blockentity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.BarShelfBlock;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlockEntityTypes {

    public static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();

    public static final BlockEntityType<CardTableBlockEntity> CARD_TABLE = register("card_table", () -> BlockEntityType.Builder.of(CardTableBlockEntity::new, ModBlocks.CARD_TABLE_MAP.values().toArray(new CardTableBlock[0])).build(null));
    public static final BlockEntityType<BarShelfBlockEntity> BAR_SHELF = register("bar_shelf", () -> BlockEntityType.Builder.of(BarShelfBlockEntity::new, ModBlocks.BAR_SHELF_MAP.values().toArray(new BarShelfBlock[0])).build(null));

    private static <E extends BlockEntity, T extends BlockEntityType<E>> T register(String id, Supplier<T> entityType) {
        T obj = entityType.get();
        BLOCK_ENTITY_TYPES.put(Charta.id(id), obj);
        return obj;
    }

    public static void register() {
        BLOCK_ENTITY_TYPES.forEach((id, entityType) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, entityType));
    }


}
