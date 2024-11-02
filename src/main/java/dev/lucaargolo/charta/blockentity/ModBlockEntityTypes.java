package dev.lucaargolo.charta.blockentity;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Charta.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CardTableBlockEntity>> CARD_TABLE = BLOCK_ENTITY_TYPES.register("card_table", () -> BlockEntityType.Builder.of(CardTableBlockEntity::new, ModBlocks.CARD_TABLE.get()).build(null));


    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }


}
