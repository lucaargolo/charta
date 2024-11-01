package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Charta.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> CARD_TABLE = ITEMS.register("card_table", () -> new BlockItem(ModBlocks.CARD_TABLE.get(), new Item.Properties()));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }


}
