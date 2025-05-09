package dev.lucaargolo.charta.blockentity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.BarShelfBlock;
import dev.lucaargolo.charta.block.CardTableBlock;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredHolder;
import net.minecraftforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Charta.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CardTableBlockEntity>> CARD_TABLE = BLOCK_ENTITY_TYPES.register("card_table", () -> BlockEntityType.Builder.of(CardTableBlockEntity::new, ModBlocks.CARD_TABLE_MAP.values().stream().map(DeferredHolder::get).toList().toArray(new CardTableBlock[0])).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarShelfBlockEntity>> BAR_SHELF = BLOCK_ENTITY_TYPES.register("bar_shelf", () -> BlockEntityType.Builder.of(BarShelfBlockEntity::new, ModBlocks.BAR_SHELF_MAP.values().stream().map(DeferredHolder::get).toList().toArray(new BarShelfBlock[0])).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }


}
