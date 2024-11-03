package dev.lucaargolo.charta.item;

import dev.lucaargolo.charta.Charta;
import dev.lucaargolo.charta.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Charta.MOD_ID);

    static {
        ModBlocks.BLOCKS.getEntries().forEach(holder -> {
            ResourceKey<Block> resourceKey = holder.getKey();
            if(resourceKey != null) {
                ITEMS.register(resourceKey.location().getPath(), () -> new BlockItem(holder.get(), new Item.Properties()));
            }
        });
    }

    public static final DeferredHolder<Item, DeckItem> DECK = ITEMS.register("deck", () -> new DeckItem(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }


}
